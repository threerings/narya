//
// $Id: ConnectionManager.java,v 1.3 2001/06/01 22:12:03 mdb Exp $

package com.threerings.cocktail.cher.server.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import ninja2.core.io_core.nbio.*;

import com.samskivert.util.LoopingThread;
import com.samskivert.util.Tuple;
import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.io.FramingOutputStream;
import com.threerings.cocktail.cher.io.TypedObjectFactory;
import com.threerings.cocktail.cher.net.DownstreamMessage;
import com.threerings.cocktail.cher.net.Registry;

/**
 * The connection manager manages the socket on which connections are
 * received. It creates connection objects to manage each individual
 * connection, but those connection objects interact closely with the
 * connection manager because network I/O is done via a poll()-like
 * mechanism rather than via threads.
 */
public class ConnectionManager extends LoopingThread
{
    /**
     * Constructs and initialized a connection manager (binding the socket
     * on which it will listen for client connections).
     *
     * @param authmgr The authentication manager to use when
     * authenticating client connections.
     */
    public ConnectionManager (AuthManager authmgr)
        throws IOException
    {
        int port = DEFAULT_CM_PORT;

        // keep a handle on our authentication manager
        _authmgr = authmgr;
        // complete the introductions
        _authmgr.setConnectionManager(this);

        // we use this to wait for activity on our sockets
        _selset = new SelectSet();

        // create our listening socket and add it to the select set
        _listener = new NonblockingServerSocket(port);
        _litem = new SelectItem(_listener, Selectable.ACCEPT_READY);
        // when an ACCEPT_READY event happens, we do this:
        _litem.obj = new NetEventHandler() {
            public void handleEvent (Selectable item, short events) {
                acceptConnection();
            }
        };
        _selset.add(_litem);

        // we'll use these for sending messages to clients
        _framer = new FramingOutputStream();
        _dout = new DataOutputStream(_framer);
    }

    /**
     * Returns a reference to the auth manager being used to authenticate
     * connections.
     */
    public AuthManager getAuthManager ()
    {
        return _authmgr;
    }

    /**
     * Adds the specified connection observer to the observers list.
     * Connection observers will be notified of connection-related
     * events. An observer will not be added to the list twice.
     *
     * @see ConnectionObserver
     */
    public void addConnectionObserver (ConnectionObserver observer)
    {
        synchronized (_observers) {
            if (!_observers.contains(observer)) {
                _observers.add(observer);
            }
        }
    }

    /**
     * Removes the specified connection observer from the observers list.
     */
    public void removeConnectionObserver (ConnectionObserver observer)
    {
        synchronized (_observers) {
            _observers.remove(observer);
        }
    }

    /**
     * Notifies the connection observers of a connection event. Used
     * internally.
     */
    protected void notifyObservers (int code, Connection conn,
                                    IOException cause)
    {
        synchronized (_observers) {
            for (int i = 0; i < _observers.size(); i++) {
                ConnectionObserver obs =
                    (ConnectionObserver)_observers.get(i);
                switch (code) {
                case CONNECTION_ESTABLISHED:
                    obs.connectionEstablished(conn);
                    break;
                case CONNECTION_FAILED:
                    obs.connectionFailed(conn, cause);
                    break;
                case CONNECTION_CLOSED:
                    obs.connectionClosed(conn);
                    break;
                default:
                    throw new RuntimeException("Invalid code supplied to " +
                                               "notifyObservers: " + code);
                }
            }
        }
    }

    protected void willStart ()
    {
        Log.info("Connection Manager thread running.");
    }

    /**
     * Performs the select loop. This is the body of the conmgr thread.
     */
    protected void iterate ()
    {
        // send any messages that are waiting on the outgoing queue
        Tuple tup;
        while ((tup = (Tuple)_outq.getNonBlocking()) != null) {
            Connection conn = (Connection)tup.left;
            DownstreamMessage outmsg = (DownstreamMessage)tup.right;
            try {
                // first flatten the message (and frame it)
                TypedObjectFactory.writeTo(_dout, outmsg);
                // then write framed message to real output stream
                _framer.writeFrameAndReset(conn.getOutputStream());

            } catch (IOException ioe) {
                connectionFailed(conn, ioe);
            }
        }

        // check for connections that have completed authentication
        Connection conn;
        while ((conn = (Connection)_authq.getNonBlocking()) != null) {
            // remove the old connection from the select set
            _selset.remove(conn.getSelectItem());

            // construct a new running connection to handle this
            // connections network traffic from here on out
            try {
                RunningConnection rconn =
                    new RunningConnection(this, conn.getSocket());
                // wire this connection up to receive network events
                _selset.add(rconn.getSelectItem());

                // and let our observers know about our new connection
                notifyObservers(CONNECTION_ESTABLISHED, rconn, null);

            } catch (IOException ioe) {
                Log.warning("Failure upgrading authing connection to " +
                            "running connection.");
                Log.logStackTrace(ioe);
            }
        }

        // check for incoming network events
        int ecount = _selset.select(SELECT_LOOP_TIME);
        if (ecount == 0) {
            return;
        }

        // process those events
        SelectItem[] active = _selset.getEvents();
        for (int i = 0; i < active.length; i++) {
            try {
                SelectItem item = active[i];
                NetEventHandler handler = (NetEventHandler)item.obj;
                handler.handleEvent(item.getSelectable(), item.revents);

            } catch (Exception e) {
                Log.warning("Error processing network data.");
                Log.logStackTrace(e);
            }
        }
    }

    protected void didShutdown ()
    {
        Log.info("Connection Manager thread exited.");
    }

    /**
     * Called by our net event handler when a new connection is ready to
     * be accepted on our listening socket.
     */
    protected void acceptConnection ()
    {
        NonblockingSocket socket = null;

        try {
            socket = _listener.nbAccept();
            if (socket == null) {
                // in theory this shouldn't happen because we got an
                // ACCEPT_READY event, but better safe than sorry
                Log.info("Psych! Got ACCEPT_READY, but no connection.");
                return;
            }

            // create a new authing connection object to manage the
            // authentication of this client connection
            AuthingConnection acon = new AuthingConnection(this, socket);

            // wire this connection into the select set
            _selset.add(acon.getSelectItem());

        } catch (IOException ioe) {
            Log.warning("Failure accepting new connection: " + ioe);

            // make sure we don't leak a socket if something went awry
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe2) {
                    Log.warning("Failed closing aborted connection: " + ioe2);
                }
            }
        }
    }

    /**
     * Called by a connection when it has a downstream message that needs
     * to be delivered.
     */
    void postMessage (Connection conn, DownstreamMessage msg)
    {
        // sanity check
        if (conn == null || msg == null) {
            Log.warning("Bogosity.");
            Thread.dumpStack();

        } else {
            // slap both these suckers onto the outgoing message queue
            _outq.append(new Tuple(conn, msg));
        }
    }

    /**
     * Called by a connection if it experiences a network failure.
     */
    void connectionFailed (Connection conn, IOException ioe)
    {
        // remove this connection from the select set
        _selset.remove(conn.getSelectItem());

        // let our observers know what's up
        notifyObservers(CONNECTION_FAILED, conn, ioe);
    }

    /**
     * Called by a connection when it discovers that it's closed.
     */
    void connectionClosed (Connection conn)
    {
        Log.info("Removing closed connection: " + conn);

        // remove this connection from the select set
        _selset.remove(conn.getSelectItem());

        // let our observers know what's up
        notifyObservers(CONNECTION_CLOSED, conn, null);
    }

    /**
     * Called by the auth manager to indicate that a connection was
     * successfully authenticated.
     */
    void connectionDidAuthenticate (Connection conn)
    {
        // slap this sucker onto the authenticated connections queue
        _authq.append(conn);
    }

    protected AuthManager _authmgr;
    protected SelectSet _selset;

    protected NonblockingServerSocket _listener;
    protected SelectItem _litem;

    protected Queue _outq = new Queue();
    protected FramingOutputStream _framer;
    protected DataOutputStream _dout;

    protected Queue _authq = new Queue();

    protected ArrayList _observers = new ArrayList();

    /** The default port on which we listen for connections. */
    protected static final int DEFAULT_CM_PORT = 4007;

    /**
     * How long we wait for network events before checking our running
     * flag to see if we should still be running.
     */
    protected static final int SELECT_LOOP_TIME = 30 * 1000;

    // codes for notifyObservers()
    protected static final int CONNECTION_ESTABLISHED = 0;
    protected static final int CONNECTION_FAILED = 1;
    protected static final int CONNECTION_CLOSED = 2;

    // register our shared objects
    static {
        Registry.registerTypedObjects();
    }
}
