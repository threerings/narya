//
// $Id: ConnectionManager.java,v 1.42 2004/08/04 03:32:18 mdb Exp $

package com.threerings.presents.server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import java.net.InetSocketAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.samskivert.util.*;

import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;

import com.threerings.presents.data.ConMgrStats;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.DownstreamMessage;

import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.PresentsServer;

/**
 * The connection manager manages the socket on which connections are
 * received. It creates connection objects to manage each individual
 * connection, but those connection objects interact closely with the
 * connection manager because network I/O is done via a poll()-like
 * mechanism rather than via threads.
 */
public class ConnectionManager extends LoopingThread
    implements PresentsServer.Reporter
{
    /**
     * Constructs and initialized a connection manager (binding the socket
     * on which it will listen for client connections).
     */
    public ConnectionManager (int port)
        throws IOException
    {
        _port = port;
        _selector = SelectorProvider.provider().openSelector();

        // create our stats record
        _stats = new ConMgrStats();
        _stats.init();

        // register as a "state of server" reporter
        PresentsServer.registerReporter(this);
    }

    /**
     * Configures the connection manager with an entity that will be
     * informed of the success or failure of the connection manager
     * initialization process. <em>Note:</em> the callback methods will be
     * called on the connection manager thread, so be careful not to do
     * anything on those methods that will conflict with activities on the
     * dobjmgr thread, etc.
     */
    public void setStartupListener (ResultListener rl)
    {
        _startlist = rl;
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
     * Returns our current runtime statistics. When the stats are fetched
     * the counters are rolled to the next bucket. 60 buckets are tracked.
     * <em>Note:</em> don't call this method <em>too</em> frequently (more
     * often than once every few seconds or so) as it has to total things
     * up and run a number of synchronized methods.
     */
    public synchronized ConMgrStats getStats ()
    {
        // fill in our snapshot values
        _stats.authQueueSize[_stats.current] = _authq.size();
        _stats.deathQueueSize[_stats.current] = _deathq.size();
        _stats.outQueueSize[_stats.current] = _outq.size();
        if (_oflowqs.size() > 0) {
            Iterator oqiter = _oflowqs.values().iterator();
            while (oqiter.hasNext()) {
                OverflowQueue oq = (OverflowQueue)oqiter.next();
                _stats.overQueueSize[_stats.current] += oq.size();
            }
        }
        _stats.increment();
        return _stats;
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
            _observers.add(observer);
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

    // documentation inherited from interface PresentsServer.Reporter
    public void appendReport (StringBuffer report, long now, long sinceLast)
    {
        long bytesIn, bytesOut, msgsIn, msgsOut;
        synchronized (this) {
            bytesIn = _bytesIn; _bytesIn = 0L;
            bytesOut = _bytesOut; _bytesOut = 0L;
            msgsIn = _msgsIn; _msgsIn = 0;
            msgsOut = _msgsOut; _msgsOut = 0;
        }

        report.append("* presents.net.ConnectionManager:\n");
        report.append("- Network input: ");
        report.append(bytesIn).append(" bytes, ");
        report.append(msgsIn).append(" msgs, ");
        long avgIn = (msgsIn == 0) ? 0 : (bytesIn/msgsIn);
        report.append(avgIn).append(" avg size, ");
        report.append(bytesIn*1000/sinceLast).append(" bps\n");
        report.append("- Network output: ");
        report.append(bytesOut).append(" bytes, ");
        report.append(msgsOut).append(" msgs, ");
        long avgOut = (msgsOut == 0) ? 0 : (bytesOut/msgsOut);
        report.append(avgOut).append(" avg size, ");
        report.append(bytesOut*1000/sinceLast).append(" bps\n");
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
        try {
            // create our listening socket and add it to the select set
            _listener = ServerSocketChannel.open();
            _listener.configureBlocking(false);

            InetSocketAddress isa = new InetSocketAddress(_port);
            _listener.socket().bind(isa);
            Log.info("Server listening on " + isa + ".");

            // register our listening socket and map its select key to a
            // net event handler that will accept new connections
            SelectionKey lkey =
                _listener.register(_selector, SelectionKey.OP_ACCEPT);
            _handlers.put(lkey, new NetEventHandler() {
                public int handleEvent (long when) {
                    acceptConnection();
                    // there's no easy way to measure bytes read when
                    // accepting a connection, so we claim nothing
                    return 0;
                }
                public boolean checkIdle (long now) {
                    return false; // we're never idle
                }
            });

        } catch (IOException ioe) {
            Log.warning("Failure listening to socket on port '" +_port + "'.");
            Log.logStackTrace(ioe);

            // notify our startup listener, if we have one
            if (_startlist != null) {
                _startlist.requestFailed(ioe);
            }
            return;
        }

        // we'll use this for sending messages to clients
        _framer = new FramingOutputStream();

        // notify our startup listener, if we have one
        if (_startlist != null) {
            _startlist.requestCompleted(null);
        }
    }

    /**
     * Performs the select loop. This is the body of the conmgr thread.
     */
    protected void iterate ()
    {
        long iterStamp = System.currentTimeMillis();

        // close any connections that have been queued up to die
        Connection dconn;
        while ((dconn = (Connection)_deathq.getNonBlocking()) != null) {
            // it's possible that we caught an EOF trying to read from
            // this connection even after it was queued up for death, so
            // let's avoid trying to close it twice
            if (!dconn.isClosed()) {
                dconn.close();
            }
        }

        // close connections that have had no network traffic for too long
        Iterator hiter = _handlers.values().iterator();
        while (hiter.hasNext()) {
            NetEventHandler handler = (NetEventHandler)hiter.next();
            if (handler.checkIdle(iterStamp)) {
                // this will queue the connection up for closure on our
                // next tick
                closeConnection((Connection)handler);
            }
        }

        // attempt to send any messages waiting on the overflow queues
        if (_oflowqs.size() > 0) {
            Iterator oqiter = _oflowqs.values().iterator();
            while (oqiter.hasNext()) {
                OverflowQueue oq = (OverflowQueue)oqiter.next();
                try {
                    // try writing the messages in this overflow queue
                    if (oq.writeOverflowMessages(iterStamp)) {
                        // if they were all written, we can remove it
                        oqiter.remove();
//                         Log.info("Flushed overflow queue for " +
//                                  oq.conn + ".");
                    }

                } catch (IOException ioe) {
                    oq.conn.handleFailure(ioe);
                }
            }
        }

        // send any messages that are waiting on the outgoing queue
        Tuple tup;
        while ((tup = (Tuple)_outq.getNonBlocking()) != null) {
            Connection conn = (Connection)tup.left;

            // if an overflow queue exists for this client, go ahead and
            // slap the message on there because we can't send it until
            // all other messages in their queue have gone out
            OverflowQueue oqueue = (OverflowQueue)_oflowqs.get(conn);
            if (oqueue != null) {
                int size = oqueue.size();
                if ((size > 500) && (size % 50 == 0)) {
                    Log.warning("Aiya, big overflow queue for " + conn +
                                " [size=" + size +
                                ", adding=" + tup.right + "].");
                }
                oqueue.add(tup.right);
                continue;
            }

            // otherwise write the message out to the client directly
            writeMessage(conn, (DownstreamMessage)tup.right, _oflowHandler);
        }

        // check for connections that have completed authentication
        AuthingConnection conn;
        while ((conn = (AuthingConnection)_authq.getNonBlocking()) != null) {
            try {
                // construct a new running connection to handle this
                // connections network traffic from here on out
                SelectionKey selkey = conn.getSelectionKey();
                RunningConnection rconn = new RunningConnection(
                    this, selkey, conn.getChannel(), iterStamp);

                // we need to keep using the same object input and output
                // streams from the beginning of the session because they
                // have contextual state that needs to be preserved
                rconn.inheritStreams(conn);

                // replace the mapping in the handlers table from the old
                // connection with the new one
                _handlers.put(selkey, rconn);

                // and let our observers know about our new connection
                notifyObservers(CONNECTION_ESTABLISHED, rconn,
                                conn.getAuthRequest(), conn.getAuthResponse());

            } catch (IOException ioe) {
                Log.warning("Failure upgrading authing connection to " +
                            "running connection.");
                Log.logStackTrace(ioe);
            }
        }

        Set ready = null;
        try {
            // check for incoming network events
//             Log.debug("Selecting from " +
//                       StringUtil.toString(_selector.keys()) + " (" +
//                       SELECT_LOOP_TIME + ").");
            int ecount = _selector.select(SELECT_LOOP_TIME);
            ready = _selector.selectedKeys();
            if (ecount == 0) {
                if (ready.size() == 0) {
                    return;
                } else {
                    Log.warning("select() returned no selected sockets, " +
                                "but there are " + ready.size() +
                                " in the ready set.");
                }
            }

        } catch (IOException ioe) {
            Log.warning("Failure select()ing [ioe=" + ioe + "].");
            return;
        }

        // process those events
//         Log.info("Ready set " + StringUtil.toString(ready) + ".");
        Iterator siter = ready.iterator();
        while (siter.hasNext()) {
            SelectionKey selkey = (SelectionKey)siter.next();
            NetEventHandler handler = null;
            try {
                handler = (NetEventHandler)_handlers.get(selkey);
                if (handler == null) {
                    Log.warning("Received network event but have no " +
                                "registered handler [selkey=" + selkey + "].");
                    continue;
                }

//                 Log.info("Got event [selkey=" + selkey +
//                          ", handler=" + handler + "].");

                int got = handler.handleEvent(iterStamp);
                if (got != 0) {
                    synchronized (this) {
                        _bytesIn += got;
                        _stats.bytesIn[_stats.current] += got;
                        // we know that the handlers only report having
                        // read bytes when they have a whole message, so
                        // we can count thusly
                        _msgsIn++;
                        _stats.msgsIn[_stats.current]++;
                    }
                }

            } catch (Exception e) {
                Log.warning("Error processing network data: " + handler + ".");
                Log.logStackTrace(e);

                // if you freak out here, you go straight in the can
                if (handler != null && handler instanceof Connection) {
                    closeConnection((Connection)handler);
                }
            }
        }
        ready.clear();
    }

    /**
     * Writes a message out to a connection, passing the buck to the
     * partial write handler if the entire message could not be written.
     *
     * @return true if the message was fully written, false if it was
     * partially written (in which case the partial message handler will
     * have been invoked).
     */
    protected boolean writeMessage (
        Connection conn, DownstreamMessage outmsg, PartialWriteHandler pwh)
    {
        // if the connection to which this message is destined is closed,
        // drop the message and move along quietly; this is perfectly
        // legal, a user can logoff whenever they like, even if we still
        // have things to tell them; such is life in a fully asynchronous
        // distributed system
        if (conn.isClosed()) {
            return true;
        }
            
        boolean fully = true;
        try {
            // write the message via the connection's object output stream
            // (which we configure to write data to our framing output
            // stream)
            ObjectOutputStream oout = conn.getObjectOutputStream(_framer);
//             Log.info("Sending " + outmsg + ".");
            oout.writeObject(outmsg);
            oout.flush();

            try {
                // then write the framed message to the socket
                ByteBuffer buffer = _framer.frameAndReturnBuffer();
                int wrote = conn.getChannel().write(buffer);
                noteWrite(1, wrote);

                if (buffer.remaining() > 0) {
                    fully = false;
//                     Log.info("Partial write [conn=" + conn +
//                              ", msg=" + StringUtil.shortClassName(outmsg) +
//                              ", wrote=" + wrote +
//                              ", size=" + buffer.limit() + "].");
                    pwh.handlePartialWrite(conn, buffer);

//                 } else if (wrote > 10000) {
//                     Log.info("Big write [conn=" + conn +
//                              ", msg=" + StringUtil.shortClassName(outmsg) +
//                              ", wrote=" + wrote + "].");
                }

            } finally {
                _framer.resetFrame();
            }

        } catch (IOException ioe) {
            // instruct the connection to deal with its failure
            conn.handleFailure(ioe);
        }

        return fully;
    }

    /** Called by {@link #writeMessage} and friends when they write data
     * over the network. */
    protected final synchronized void noteWrite (int msgs, int bytes)
    {
        _msgsOut += msgs;
        _bytesOut += bytes;
        _stats.msgsOut[_stats.current] += msgs;
        _stats.bytesOut[_stats.current] += bytes;
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
        SocketChannel channel = null;

        try {
            channel = _listener.accept();
            if (channel == null) {
                // in theory this shouldn't happen because we got an
                // ACCEPT_READY event, but better safe than sorry
                Log.info("Psych! Got ACCEPT_READY, but no connection.");
                return;
            }

            if (!(channel instanceof SelectableChannel)) {
                Log.warning("Provided with un-selectable socket as " +
                            "result of accept(), can't cope " +
                            "[channel=" + channel + "].");
                // stick a fork in the socket
                channel.socket().close();
                return;
            }

            Log.debug("Accepted connection " + channel + ".");

            // create a new authing connection object to manage the
            // authentication of this client connection and register it
            // with our selection set
            SelectableChannel selchan = (SelectableChannel)channel;
            selchan.configureBlocking(false);
            SelectionKey selkey = selchan.register(
                _selector, SelectionKey.OP_READ);
            _handlers.put(selkey, new AuthingConnection(this, selkey, channel));
            return;

        } catch (IOException ioe) {
            Log.warning("Failure accepting new connection: " + ioe);
        }

        // make sure we don't leak a socket if something went awry
        if (channel != null) {
            try {
                channel.socket().close();
            } catch (IOException ioe) {
                Log.warning("Failed closing aborted connection: " + ioe);
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
        // remove this connection from our mapping (it is automatically
        // removed from the Selector when the socket is closed)
        _handlers.remove(conn.getSelectionKey());
        _oflowqs.remove(conn);

        // let our observers know what's up
        notifyObservers(CONNECTION_FAILED, conn, ioe, null);
    }

    /**
     * Called by a connection when it discovers that it's closed.
     */
    void connectionClosed (Connection conn)
    {
        // remove this connection from our mapping (it is automatically
        // removed from the Selector when the socket is closed)
        _handlers.remove(conn.getSelectionKey());
        _oflowqs.remove(conn);

        // let our observers know what's up
        notifyObservers(CONNECTION_CLOSED, conn, null, null);
    }

    /** Used to handle partial writes in {@link #writeMessage}. */
    protected static interface PartialWriteHandler
    {
        public void handlePartialWrite (Connection conn, ByteBuffer buffer);
    }

    /**
     * Used to handle messages for a client whose network buffer has
     * filled up because their outgoing network buffer has filled up. This
     * can happen if the client receives many messages in rapid succession
     * or if they receive very large messages or if they become
     * unresponsive and stop acknowledging network packets sent by the
     * server. We want to accomodate the first to circumstances and
     * recognize the third as quickly as possible so that we can
     * disconnect the client and propagate that information up to the
     * higher levels so that further messages are not queued up for the
     * unresponsive client.
     */
    protected class OverflowQueue extends ArrayList
        implements PartialWriteHandler
    {
        /** The connection for which we're managing overflow. */
        public Connection conn;

        /**
         * Creates a new overflow queue for the supplied connection and
         * with the supplied initial partial message.
         */
        public OverflowQueue (Connection conn, ByteBuffer message)
        {
            this.conn = conn;
            // set up our initial _partial buffer
            handlePartialWrite(conn, message);
        }

        /**
         * Called each time through the {@link ConnectionManager#iterate}
         * loop, this attempts to send any remaining partial message and
         * all subsequent messages in the overflow queue.
         *
         * @return true if all messages in this queue were successfully
         * sent, false if there remains data to be sent on the next loop.
         *
         * @throws IOException if an error occurs writing data to the
         * connection or if we have been unable to write any data to the
         * connection for ten seconds.
         */
        public boolean writeOverflowMessages (long iterStamp)
            throws IOException
        {
            // write any partial message if we have one
            if (_partial != null) {
                // write all we can of our partial buffer
                int wrote = conn.getChannel().write(_partial);
                noteWrite(0, wrote);

                if (_partial.remaining() == 0) {
                    _partial = null;
                } else {
//                     Log.info("Still going [conn=" + conn +
//                              ", wrote=" + wrote +
//                              ", remain=" + _partial.remaining() + "].");
                    return false;
                }
            }

            while (size() > 0) {
                DownstreamMessage outmsg = (DownstreamMessage)remove(0);
                // if any of these messages are partially written, we have
                // to stop and wait for the next tick
                if (!writeMessage(conn, outmsg, this)) {
                    return false;
                }
            }

            return true;
        }

        // documentation inherited
        public void handlePartialWrite (Connection conn, ByteBuffer buffer)
        {
            // set up our _partial buffer
            _partial = ByteBuffer.allocate(buffer.remaining());
            _partial.put(buffer);
            _partial.flip();
        }

        /** The remains of a message that was only partially written on
         * its first attempt. */
        protected ByteBuffer _partial;
    }

    protected int _port;
    protected Authenticator _author;
    protected Selector _selector;
    protected ServerSocketChannel _listener;
    protected ResultListener _startlist;

    /** Maps selection keys to network event handlers. */
    protected HashMap _handlers = new HashMap();

    protected Queue _deathq = new Queue();
    protected Queue _authq = new Queue();

    protected Queue _outq = new Queue();
    protected FramingOutputStream _framer;

    protected HashMap _oflowqs = new HashMap();

    protected ArrayList _observers = new ArrayList();

    /** Bytes in and out in the last reporting period. */
    protected long _bytesIn, _bytesOut;

    /** Messages read and written in the last reporting period. */
    protected int _msgsIn, _msgsOut;

    /** Our current runtime stats. */
    protected ConMgrStats _stats;

    /** Used to create an overflow queue on the first partial write. */
    protected PartialWriteHandler _oflowHandler = new PartialWriteHandler() {
        public void handlePartialWrite (Connection conn, ByteBuffer msgbuf) {
            // if we couldn't write all the data for this message, we'll
            // need to establish an overflow queue
            _oflowqs.put(conn, new OverflowQueue(conn, msgbuf));
        }
    };

    /**
     * How long we wait for network events before checking our running
     * flag to see if we should still be running. We don't want to loop
     * too tightly, but we need to make sure we don't sit around listening
     * for incoming network events too long when there are outgoing
     * messages in the queue.
     */
    protected static final int SELECT_LOOP_TIME = 100;

    // codes for notifyObservers()
    protected static final int CONNECTION_ESTABLISHED = 0;
    protected static final int CONNECTION_FAILED = 1;
    protected static final int CONNECTION_CLOSED = 2;
}
