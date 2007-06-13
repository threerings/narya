//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
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
import java.util.logging.Level;

import com.samskivert.util.*;

import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.data.ConMgrStats;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.DownstreamMessage;

import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.PresentsServer;

import static com.threerings.presents.Log.log;

/**
 * The connection manager manages the socket on which connections are received. It creates
 * connection objects to manage each individual connection, but those connection objects interact
 * closely with the connection manager because network I/O is done via a poll()-like mechanism
 * rather than via threads.
 */
public class ConnectionManager extends LoopingThread
    implements PresentsServer.Reporter
{
    /**
     * Constructs and initialized a connection manager (binding the socket on which it will listen
     * for client connections).
     */
    public ConnectionManager (int port)
        throws IOException
    {
        this(new int[] { port });
    }

    /**
     * Constructs and initialized a connection manager (binding socket on which it will listen for
     * client connections to each of the specified ports).
     */
    public ConnectionManager (int[] ports)
        throws IOException
    {
        _ports = ports;
        _selector = SelectorProvider.provider().openSelector();

        // create our stats record
        _stats = new ConMgrStats();
        _lastStats = new ConMgrStats();

        // register as a "state of server" reporter
        PresentsServer.registerReporter(this);
    }

    /**
     * Configures the connection manager with an entity that will be informed of the success or
     * failure of the connection manager initialization process. <em>Note:</em> the callback
     * methods will be called on the connection manager thread, so be careful not to do anything on
     * those methods that will conflict with activities on the dobjmgr thread, etc.
     */
    public void setStartupListener (ResultListener<Object> rl)
    {
        _startlist = rl;
    }

    /**
     * Specifies the authenticator that should be used by the connection manager to authenticate
     * logon requests.
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
     * Instructs us to execute the specified runnable when the connection manager thread exits.
     * <em>Note:</em> this will be executed on the connection manager thread, so don't do anything
     * dangerous. Only one action may be specified and it may be cleared by calling this method
     * with null.
     */
    public void setShutdownAction (Runnable onExit)
    {
        _onExit = onExit;
    }

    /**
     * Returns our current runtime statistics. <em>Note:</em> don't call this method <em>too</em>
     * frequently as it is synchronized and will contend with the network I/O thread.
     */
    public synchronized ConMgrStats getStats ()
    {
        // fill in our snapshot values
        _stats.authQueueSize = _authq.size();
        _stats.deathQueueSize = _deathq.size();
        _stats.outQueueSize = _outq.size();
        if (_oflowqs.size() > 0) {
            _stats.overQueueSize = 0;
            for (OverflowQueue oq : _oflowqs.values()) {
                _stats.overQueueSize += oq.size();
            }
        }
        return (ConMgrStats)_stats.clone();
    }

    /**
     * Adds the specified connection observer to the observers list.  Connection observers will be
     * notified of connection-related events. An observer will not be added to the list twice.
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
     * Called by the authenticator to indicate that a connection was successfully authenticated.
     */
    public void connectionDidAuthenticate (AuthingConnection conn)
    {
        // slap this sucker onto the authenticated connections queue
        _authq.append(conn);
    }

    // documentation inherited from interface PresentsServer.Reporter
    public void appendReport (
        StringBuilder report, long now, long sinceLast, boolean reset)
    {
        ConMgrStats stats = getStats();
        int connects = stats.connects - _lastStats.connects;
        int disconnects = stats.disconnects - _lastStats.disconnects;
        long bytesIn = stats.bytesIn - _lastStats.bytesIn;
        long bytesOut = stats.bytesOut - _lastStats.bytesOut;
        long msgsIn = stats.msgsIn - _lastStats.msgsIn;
        long msgsOut = stats.msgsOut - _lastStats.msgsOut;
        if (reset) {
            _lastStats = stats;
        }

        // make sure we don't div0 if this method somehow gets called twice in
        // the same millisecond
        sinceLast = Math.max(sinceLast, 1L);

        report.append("* presents.net.ConnectionManager:\n");
        report.append("- Network connections: ");
        report.append(connects).append(" connects, ");
        report.append(disconnects).append(" disconnects\n");
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
     * Notifies the connection observers of a connection event. Used internally.
     */
    protected void notifyObservers (
        int code, Connection conn, Object arg1, Object arg2)
    {
        synchronized (_observers) {
            for (ConnectionObserver obs : _observers) {
                switch (code) {
                case CONNECTION_ESTABLISHED:
                    obs.connectionEstablished(conn, (AuthRequest)arg1, (AuthResponse)arg2);
                    break;
                case CONNECTION_FAILED:
                    obs.connectionFailed(conn, (IOException)arg1);
                    break;
                case CONNECTION_CLOSED:
                    obs.connectionClosed(conn);
                    break;
                default:
                    throw new RuntimeException("Invalid code supplied to notifyObservers: " + code);
                }
            }
        }
    }

    // documentation inherited
    protected void willStart ()
    {
        int successes = 0;
        IOException _failure = null;
        for (int ii = 0; ii < _ports.length; ii++) {
            try {
                // create a listening socket and add it to the select set
                _ssocket = ServerSocketChannel.open();
                _ssocket.configureBlocking(false);

                InetSocketAddress isa = new InetSocketAddress(_ports[ii]);
                _ssocket.socket().bind(isa);
                registerChannel(_ssocket);
                successes++;
                log.info("Server listening on " + isa + ".");

            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failure listening to socket on port '" +
                        _ports[ii] + "'.", ioe);
                _failure = ioe;
            }
        }

        // NOTE: this is not currently working; it works but for whatever inscrutable reason the
        // inherited channel claims to be readable immediately every time through the select() loop
        // which causes the server to consume 100% of the CPU repeatedly ignoring the inherited
        // channel (except when an actual connection comes in in which case it does the right
        // thing)

//         // now look to see if we were passed a socket inetd style by a
//         // privileged parent process
//         try {
//             Channel inherited = System.inheritedChannel();
//             if (inherited instanceof ServerSocketChannel) {
//                 _ssocket = (ServerSocketChannel)inherited;
//                 _ssocket.configureBlocking(false);
//                 registerChannel(_ssocket);
//                 successes++;
//                 log.info("Server listening on " +
//                          _ssocket.socket().getInetAddress() + ":" +
//                          _ssocket.socket().getLocalPort() + ".");

//             } else if (inherited != null) {
//                 log.warning("Inherited non-server-socket channel " + inherited + ".");
//             }
//         } catch (IOException ioe) {
//             log.warning("Failed to check for inherited channel.");
//         }

        // if we failed to listen on at least one port, give up the ghost
        if (successes == 0) {
            if (_startlist != null) {
                _startlist.requestFailed(_failure);
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

    /** Helper function for {@link #willStart}. */
    protected void registerChannel (final ServerSocketChannel listener)
        throws IOException
    {
        // register this listening socket and map its select key to a net event handler that will
        // accept new connections
        SelectionKey sk = listener.register(_selector, SelectionKey.OP_ACCEPT);
        _handlers.put(sk, new NetEventHandler() {
            public int handleEvent (long when) {
                acceptConnection(listener);
                // there's no easy way to measure bytes read when accepting a connection, so we
                // claim nothing
                return 0;
            }
            public boolean checkIdle (long now) {
                return false; // we're never idle
            }
        });
    }

    /**
     * Performs the select loop. This is the body of the conmgr thread.
     */
    protected void iterate ()
    {
        long iterStamp = System.currentTimeMillis();

        // close any connections that have been queued up to die
        Connection dconn;
        while ((dconn = _deathq.getNonBlocking()) != null) {
            // it's possible that we caught an EOF trying to read from this connection even after
            // it was queued up for death, so let's avoid trying to close it twice
            if (!dconn.isClosed()) {
                dconn.close();
            }
        }

        // close connections that have had no network traffic for too long
        for (NetEventHandler handler : _handlers.values()) {
            if (handler.checkIdle(iterStamp)) {
                // this will queue the connection for closure on our next tick
                closeConnection((Connection)handler);
            }
        }

        // attempt to send any messages waiting on the overflow queues
        if (_oflowqs.size() > 0) {
            Iterator<OverflowQueue> oqiter = _oflowqs.values().iterator();
            while (oqiter.hasNext()) {
                OverflowQueue oq = oqiter.next();
                try {
                    // try writing the messages in this overflow queue
                    if (oq.writeOverflowMessages(iterStamp)) {
                        // if they were all written, we can remove it
                        oqiter.remove();
                    }

                } catch (IOException ioe) {
                    oq.conn.handleFailure(ioe);
                }
            }
        }

        // send any messages that are waiting on the outgoing queue
        Tuple<Connection,byte[]> tup;
        while ((tup = _outq.getNonBlocking()) != null) {
            Connection conn = tup.left;

            // if an overflow queue exists for this client, go ahead and slap the message on there
            // because we can't send it until all other messages in their queue have gone out
            OverflowQueue oqueue = _oflowqs.get(conn);
            if (oqueue != null) {
                int size = oqueue.size();
                if ((size > 500) && (size % 50 == 0)) {
                    log.warning("Aiya, big overflow queue for " + conn + " [size=" + size +
                                ", adding=" + tup.right + "].");
                }
                oqueue.add(tup.right);
                continue;
            }

            // otherwise write the message out to the client directly
            writeMessage(conn, tup.right, _oflowHandler);
        }

        // check for connections that have completed authentication
        AuthingConnection conn;
        while ((conn = _authq.getNonBlocking()) != null) {
            try {
                // construct a new running connection to handle this connections network traffic
                // from here on out
                SelectionKey selkey = conn.getSelectionKey();
                RunningConnection rconn = new RunningConnection(
                    this, selkey, conn.getChannel(), iterStamp);

                // we need to keep using the same object input and output streams from the
                // beginning of the session because they have context that needs to be preserved
                rconn.inheritStreams(conn);

                // replace the mapping in the handlers table from the old conn with the new one
                _handlers.put(selkey, rconn);

                // transfer any overflow queue for that connection
                OverflowQueue oflowHandler = _oflowqs.remove(conn);
                if (oflowHandler != null) {
                    _oflowqs.put(rconn, oflowHandler);
                }

                // and let our observers know about our new connection
                notifyObservers(CONNECTION_ESTABLISHED, rconn,
                                conn.getAuthRequest(), conn.getAuthResponse());

            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failure upgrading authing connection to running.", ioe);
            }
        }

        Set<SelectionKey> ready = null;
        try {
            // check for incoming network events
//             log.fine("Selecting from " + StringUtil.toString(_selector.keys()) + " (" +
//                       SELECT_LOOP_TIME + ").");
            int ecount = _selector.select(SELECT_LOOP_TIME);
            ready = _selector.selectedKeys();
            if (ecount == 0) {
                if (ready.size() == 0) {
                    return;
                } else {
                    log.warning("select() returned no selected sockets, but there are " +
                                ready.size() + " in the ready set.");
                }
            }

        } catch (IOException ioe) {
            if ("Invalid argument".equals(ioe.getMessage())) {
                // what is this, anyway?
                log.log(Level.WARNING, "Failure select()ing.", ioe);
            } else {
                log.warning("Failure select()ing [ioe=" + ioe + "].");
            }
            return;

        } catch (RuntimeException re) {
            // this block of code deals with a bug in the _selector that we observed on 2005-05-02,
            // instead of looping indefinitely after things go pear-shaped, shut us down in an
            // orderly fashion
            log.log(Level.WARNING, "Failure select()ing.", re);
            if (_runtimeExceptionCount++ >= 20) {
                log.warning("Too many errors, bailing.");
                shutdown();
            }
            return;
        }
        // clear the runtime error count
        _runtimeExceptionCount = 0;

        // process those events
//         log.info("Ready set " + StringUtil.toString(ready) + ".");
        for (SelectionKey selkey : ready) {
            NetEventHandler handler = null;
            try {
                handler = _handlers.get(selkey);
                if (handler == null) {
                    log.warning("Received network event but have no registered handler " +
                                "[selkey=" + selkey + "].");
                    // request that this key be removed from our selection set, which normally
                    // happens automatically but for some reason didn't
                    selkey.cancel();
                    continue;
                }

//                 log.info("Got event [selkey=" + selkey + ", handler=" + handler + "].");

                int got = handler.handleEvent(iterStamp);
                if (got != 0) {
                    synchronized (this) {
                        _bytesIn += got;
                        _stats.bytesIn += got;
                        // we know that the handlers only report having read bytes when they have a
                        // whole message, so we can count thusly
                        _msgsIn++;
                        _stats.msgsIn++;
                    }
                }

            } catch (Exception e) {
                log.log(Level.WARNING, "Error processing network data: " + handler + ".", e);

                // if you freak out here, you go straight in the can
                if (handler != null && handler instanceof Connection) {
                    closeConnection((Connection)handler);
                }
            }
        }
        ready.clear();
    }

    /**
     * Writes a message out to a connection, passing the buck to the partial write handler if the
     * entire message could not be written.
     *
     * @return true if the message was fully written, false if it was partially written (in which
     * case the partial message handler will have been invoked).
     */
    protected boolean writeMessage (Connection conn, byte[] data, PartialWriteHandler pwh)
    {
        // if the connection to which this message is destined is closed, drop the message and move
        // along quietly; this is perfectly legal, a user can logoff whenever they like, even if we
        // still have things to tell them; such is life in a fully asynchronous distributed system
        if (conn.isClosed()) {
            return true;
        }

        // sanity check the message size
        if (data.length > 1024 * 1024) {
            log.warning("Refusing to write absurdly large message [conn=" + conn +
                        ", size=" + data.length + "].");
            return true;
        }

        // expand our output buffer if needed to accomodate this message
        if (data.length > _outbuf.capacity()) {
            // increase the buffer size in large increments
            int ncapacity = Math.max(_outbuf.capacity() << 1, data.length);
            log.info("Expanding output buffer size [nsize=" + ncapacity + "].");
            _outbuf = ByteBuffer.allocateDirect(ncapacity);
	}

        boolean fully = true;
        try {
//             log.info("Writing " + data.length + " byte message to " + conn + ".");

            // first copy the data into our "direct" output buffer
            _outbuf.put(data);
            _outbuf.flip();

            // then write the data to the socket
            int wrote = conn.getChannel().write(_outbuf);
            noteWrite(1, wrote);

            if (_outbuf.remaining() > 0) {
                fully = false;
//                 log.info("Partial write [conn=" + conn +
//                          ", msg=" + StringUtil.shortClassName(outmsg) + ", wrote=" + wrote +
//                          ", size=" + buffer.limit() + "].");
                pwh.handlePartialWrite(conn, _outbuf);

//                 } else if (wrote > 10000) {
//                     log.info("Big write [conn=" + conn +
//                              ", msg=" + StringUtil.shortClassName(outmsg) +
//                              ", wrote=" + wrote + "].");
            }

        } catch (IOException ioe) {
            // instruct the connection to deal with its failure
            conn.handleFailure(ioe);

        } finally {
            _outbuf.clear();
        }

        return fully;
    }

    /** Called by {@link #writeMessage} and friends when they write data over the network. */
    protected final synchronized void noteWrite (int msgs, int bytes)
    {
        _msgsOut += msgs;
        _bytesOut += bytes;
        _stats.msgsOut += msgs;
        _stats.bytesOut += bytes;
    }

    // documentation inherited
    protected void handleIterateFailure (Exception e)
    {
        // log the exception
        log.log(Level.WARNING, "ConnectionManager.iterate() uncaught exception.", e);
    }

    // documentation inherited
    protected void didShutdown ()
    {
        // unbind our listening socket
        try {
            _ssocket.socket().close();
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Failed to close listening socket.", ioe);
        }

        // run our on-exit handler if we have one
        Runnable onExit = _onExit;
        if (onExit != null) {
            log.info("Connection Manager thread exited (running onExit).");
            onExit.run();
        } else {
            log.info("Connection Manager thread exited.");
        }
    }

    /**
     * Called by our net event handler when a new connection is ready to be accepted on our
     * listening socket.
     */
    protected void acceptConnection (ServerSocketChannel listener)
    {
        SocketChannel channel = null;

        try {
            channel = listener.accept();
            if (channel == null) {
                // in theory this shouldn't happen because we got an ACCEPT_READY event, but better
                // safe than sorry
                log.info("Psych! Got ACCEPT_READY, but no connection.");
                return;
            }

            if (!(channel instanceof SelectableChannel)) {
                try {
                    log.warning("Provided with un-selectable socket as result of accept(), can't " +
                                "cope [channel=" + channel + "].");
                } catch (Error err) {
                    log.warning("Un-selectable channel also couldn't be printed.");
                }
                // stick a fork in the socket
                channel.socket().close();
                return;
            }

//             log.fine("Accepted connection " + channel + ".");

            // create a new authing connection object to manage the authentication of this client
            // connection and register it with our selection set
            SelectableChannel selchan = (SelectableChannel)channel;
            selchan.configureBlocking(false);
            SelectionKey selkey = selchan.register(_selector, SelectionKey.OP_READ);
            _handlers.put(selkey, new AuthingConnection(this, selkey, channel));
            synchronized (this) {
                _stats.connects++;
            }
            return;

        } catch (IOException ioe) {
            // no need to complain this happens in the normal course of events
//             log.log(Level.WARNING, "Failure accepting new connection.", ioe);
        }

        // make sure we don't leak a socket if something went awry
        if (channel != null) {
            try {
                channel.socket().close();
            } catch (IOException ioe) {
                log.warning("Failed closing aborted connection: " + ioe);
            }
        }
    }

    /**
     * Called by a connection when it has a downstream message that needs to be delivered.
     * <em>Note:</em> this method is called as a result of a call to {@link Connection#postMessage}
     * which happens when forwarding an event to a client and at the completion of authentication,
     * both of which <em>must</em> happen only on the distributed object thread.
     */
    void postMessage (Connection conn, DownstreamMessage msg)
    {
        // sanity check
        if (conn == null || msg == null) {
            log.warning("postMessage() bogosity [conn=" + conn + ", msg=" + msg + "].");
            Thread.dumpStack();
            return;
        }

        // more sanity check; messages must only be posted from the dobjmgr thread
        if (!PresentsServer.omgr.isDispatchThread()) {
            log.warning("Message posted on non-distributed object thread [conn=" + conn +
                        ", msg=" + msg + ", thread=" + Thread.currentThread() + "].");
            Thread.dumpStack();
            // let it through though as we don't want to break things unnecessarily
        }

        try {
            _framer.resetFrame();

            // flatten this message using the connection's output stream
            ObjectOutputStream oout = conn.getObjectOutputStream(_framer);
            oout.writeObject(msg);
            oout.flush();

            // now extract that data into a byte array
            ByteBuffer buffer = _framer.frameAndReturnBuffer();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);

//             log.info("Flattened " + msg + " into " + data.length + " bytes.");

            // and slap both on the queue
            _outq.append(new Tuple<Connection,byte[]>(conn, data));

        } catch (Exception e) {
            log.log(Level.WARNING, "Failure flattening message [conn=" + conn +
                    ", msg=" + msg + "].", e);
        }
    }

    /**
     * Called by a connection if it experiences a network failure.
     */
    void connectionFailed (Connection conn, IOException ioe)
    {
        // remove this connection from our mapping (it is automatically removed from the Selector
        // when the socket is closed)
        _handlers.remove(conn.getSelectionKey());
        _oflowqs.remove(conn);
        synchronized (this) {
            _stats.disconnects++;
        }

        // let our observers know what's up
        notifyObservers(CONNECTION_FAILED, conn, ioe, null);
    }

    /**
     * Called by a connection when it discovers that it's closed.
     */
    void connectionClosed (Connection conn)
    {
        // remove this connection from our mapping (it is automatically removed from the Selector
        // when the socket is closed)
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
     * Used to handle messages for a client whose network buffer has filled up because their
     * outgoing network buffer has filled up. This can happen if the client receives many messages
     * in rapid succession or if they receive very large messages or if they become unresponsive
     * and stop acknowledging network packets sent by the server. We want to accomodate the first
     * to circumstances and recognize the third as quickly as possible so that we can disconnect
     * the client and propagate that information up to the higher levels so that further messages
     * are not queued up for the unresponsive client.
     */
    protected class OverflowQueue extends ArrayList<byte[]>
        implements PartialWriteHandler
    {
        /** The connection for which we're managing overflow. */
        public Connection conn;

        /**
         * Creates a new overflow queue for the supplied connection and with the supplied initial
         * partial message.
         */
        public OverflowQueue (Connection conn, ByteBuffer message)
        {
            this.conn = conn;
            // set up our initial _partial buffer
            handlePartialWrite(conn, message);
        }

        /**
         * Called each time through the {@link ConnectionManager#iterate} loop, this attempts to
         * send any remaining partial message and all subsequent messages in the overflow queue.
         *
         * @return true if all messages in this queue were successfully sent, false if there
         * remains data to be sent on the next loop.
         *
         * @throws IOException if an error occurs writing data to the connection or if we have been
         * unable to write any data to the connection for ten seconds.
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
                    _partials++;
                } else {
//                     log.info("Still going [conn=" + conn + ", wrote=" + wrote +
//                              ", remain=" + _partial.remaining() + "].");
                    return false;
                }
            }

            while (size() > 0) {
                byte[] data = remove(0);
                // if any of these messages are partially written, we have to stop and wait for the
                // next tick
                _msgs++;
                if (!writeMessage(conn, data, this)) {
                    return false;
                }
            }

            return true;
        }

        // documentation inherited
        public void handlePartialWrite (Connection conn, ByteBuffer buffer)
        {
            // set up our _partial buffer
            _partial = ByteBuffer.allocateDirect(buffer.remaining());
            _partial.put(buffer);
            _partial.flip();
        }

        /**
         * Returns a string representation of this instance.
         */
        public String toString ()
        {
            return "[conn=" + conn + ", partials=" + _partials + ", msgs=" + _msgs + "]";
        }

        /** The remains of a message that was only partially written on its first attempt. */
        protected ByteBuffer _partial;

        /** A couple of counters. */
        protected int _msgs, _partials;
    }

    protected int[] _ports;
    protected Authenticator _author;
    protected Selector _selector;
    protected ServerSocketChannel _ssocket;
    protected ResultListener<Object> _startlist;

    /** Counts consecutive runtime errors in select(). */
    protected int _runtimeExceptionCount;

    /** Maps selection keys to network event handlers. */
    protected HashMap<SelectionKey,NetEventHandler> _handlers =
        new HashMap<SelectionKey,NetEventHandler>();

    protected Queue<Connection> _deathq = new Queue<Connection>();
    protected Queue<AuthingConnection> _authq = new Queue<AuthingConnection>();

    protected Queue<Tuple<Connection,byte[]>> _outq = new Queue<Tuple<Connection,byte[]>>();
    protected FramingOutputStream _framer;
    protected ByteBuffer _outbuf = ByteBuffer.allocateDirect(64 * 1024);

    protected HashMap<Connection,OverflowQueue> _oflowqs = new HashMap<Connection,OverflowQueue>();

    protected ArrayList<ConnectionObserver> _observers = new ArrayList<ConnectionObserver>();

    /** Bytes in and out in the last reporting period. */
    protected long _bytesIn, _bytesOut;

    /** Messages read and written in the last reporting period. */
    protected int _msgsIn, _msgsOut;

    /** Our current runtime stats. */
    protected ConMgrStats _stats;

    /** A snapshot of our runtime stats as of our last report. */
    protected ConMgrStats _lastStats;

    /** A runnable to execute when the connection manager thread exits. */
    protected volatile Runnable _onExit;

    /** Used to create an overflow queue on the first partial write. */
    protected PartialWriteHandler _oflowHandler = new PartialWriteHandler() {
        public void handlePartialWrite (Connection conn, ByteBuffer msgbuf) {
            // if we couldn't write all the data for this message, we'll need to establish an
            // overflow queue
            _oflowqs.put(conn, new OverflowQueue(conn, msgbuf));
        }
    };

    /** How long we wait for network events before checking our running flag to see if we should
     * still be running. We don't want to loop too tightly, but we need to make sure we don't sit
     * around listening for incoming network events too long when there are outgoing messages in
     * the queue. */
    protected static final int SELECT_LOOP_TIME = 100;

    // codes for notifyObservers()
    protected static final int CONNECTION_ESTABLISHED = 0;
    protected static final int CONNECTION_FAILED = 1;
    protected static final int CONNECTION_CLOSED = 2;
}
