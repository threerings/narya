//
// $Id: ConnectionManager.java,v 1.18 2002/03/28 22:32:32 mdb Exp $

package com.threerings.presents.server.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ninja2.core.io_core.nbio.*;
import com.samskivert.util.*;

import com.threerings.presents.Log;
import com.threerings.presents.client.Client;

import com.threerings.presents.io.FramingOutputStream;
import com.threerings.presents.io.TypedObjectFactory;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.DownstreamMessage;

import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.PresentsConfig;
import com.threerings.presents.server.PresentsServer;

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
     * @param config A config object from which the connection manager
     * will fetch its configuration parameters.
     */
    public ConnectionManager ()
        throws IOException
    {
        // the listen port is specified in our configuration
        _port = PresentsConfig.config.getValue(
            CM_PORT_KEY, Client.DEFAULT_SERVER_PORT);

        // we use this to wait for activity on our sockets
        _selset = new SelectSet();

        // create our listening socket and add it to the select set
        _listener = new NonblockingServerSocket(_port);
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
     * Specifies the authenticator that should be used by the connection
     * manager to authenticate logon requests.
     */
    public void setAuthenticator (Authenticator author)
    {
        // say hello to our new authenticator
        _author = author;
        _author.setConnectionManager(this);
    }

    /**
     * Returns the entity that is being used to authenticate connections.
     */
    public Authenticator getAuthenticator ()
    {
        return _author;
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
     * Queues a connection up to be closed on the conmgr thread.
     */
    public void closeConnection (Connection conn)
    {
        _deathq.append(conn);
    }

    /**
     * Called by the authenticator to indicate that a connection was
     * successfully authenticated.
     */
    public void connectionDidAuthenticate (Connection conn)
    {
        // slap this sucker onto the authenticated connections queue
        _authq.append(conn);
    }

    /**
     * Notifies the connection observers of a connection event. Used
     * internally.
     */
    protected void notifyObservers (
        int code, Connection conn, Object arg1, Object arg2)
    {
        synchronized (_observers) {
            for (int i = 0; i < _observers.size(); i++) {
                ConnectionObserver obs =
                    (ConnectionObserver)_observers.get(i);
                switch (code) {
                case CONNECTION_ESTABLISHED:
                    obs.connectionEstablished(conn, (AuthRequest)arg1,
                                              (AuthResponse)arg2);
                    break;
                case CONNECTION_FAILED:
                    obs.connectionFailed(conn, (IOException)arg1);
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

    // documentation inherited
    protected void willStart ()
    {
        Log.info("Connection Manager listening [port=" + _port + "].");
    }

    /**
     * Performs the select loop. This is the body of the conmgr thread.
     */
    protected void iterate ()
    {
        // close any connections that have been queued up to die
        Connection dconn;
        while ((dconn = (Connection)_deathq.getNonBlocking()) != null) {
            dconn.close();
        }

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
        AuthingConnection conn;
        while ((conn = (AuthingConnection)_authq.getNonBlocking()) != null) {
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
                notifyObservers(CONNECTION_ESTABLISHED, rconn,
                                conn.getAuthRequest(), conn.getAuthResponse());

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

    // documentation inherited
    protected void handleIterateFailure (Exception e)
    {
        // log the exception
        Log.warning("ConnectionManager.iterate() uncaught exception.");
        Log.logStackTrace(e);
    }

    // documentation inherited
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
                // Log.info("Psych! Got ACCEPT_READY, but no connection.");
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
        notifyObservers(CONNECTION_FAILED, conn, ioe, null);
    }

    /**
     * Called by a connection when it discovers that it's closed.
     */
    void connectionClosed (Connection conn)
    {
        // remove this connection from the select set
        _selset.remove(conn.getSelectItem());

        // let our observers know what's up
        notifyObservers(CONNECTION_CLOSED, conn, null, null);
    }

    protected int _port;
    protected Authenticator _author;
    protected SelectSet _selset;

    protected NonblockingServerSocket _listener;
    protected SelectItem _litem;

    protected Queue _deathq = new Queue();

    protected Queue _outq = new Queue();
    protected FramingOutputStream _framer;
    protected DataOutputStream _dout;

    protected Queue _authq = new Queue();

    protected ArrayList _observers = new ArrayList();

    /** The config key for our listening port. */
    protected static final String CM_PORT_KEY = "conmgr_port";

    /**
     * How long we wait for network events before checking our running
     * flag to see if we should still be running.
     */
    // protected static final int SELECT_LOOP_TIME = 30 * 1000;

    // while we're testing, use a short loop time
    protected static final int SELECT_LOOP_TIME = 10;

    // codes for notifyObservers()
    protected static final int CONNECTION_ESTABLISHED = 0;
    protected static final int CONNECTION_FAILED = 1;
    protected static final int CONNECTION_CLOSED = 2;
}
