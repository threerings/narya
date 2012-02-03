//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.nio.conman;

import java.util.ArrayList;
import java.util.Map;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;
import com.samskivert.util.Tuple;

import com.threerings.nio.SelectorIterable;

import static com.threerings.NaryaLog.log;

/**
 * Manages socket connections. It creates connection objects for each socket connection, but those
 * connection objects interact closely with the connection manager because network I/O is done via
 * a poll()-like mechanism rather than via threads.<p>
 *
 * ConnectionManager doesn't directly accept TCP connections; it expects
 * {@link ServerSocketChannelAcceptor} or an external entity to do so and call its
 * {@link #handleAcceptedSocket} method
 */
public abstract class ConnectionManager extends LoopingThread
    implements Lifecycle.ShutdownComponent
{
    /**
     * Creates a connection manager instance.
     */
    public ConnectionManager (Lifecycle cycle, long idleTime)
        throws IOException
    {
        super("ConnectionManager");
        cycle.addComponent(this);
        _selector = Selector.open();
        _idleTime = idleTime;
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
        _stats.connectionCount = _connections.size();
        _stats.handlerCount = _handlers.size();
        _stats.deathQueueSize = _deathq.size();
        _stats.outQueueSize = _outq.size();
        if (_oflowqs.size() > 0) {
            _stats.overQueueSize = 0;
            for (OverflowQueue oq : _oflowqs.values()) {
                _stats.overQueueSize += oq.size();
            }
        }
        return _stats.clone();
    }

    /**
     * Registers <code>ops</code> on <code>chan</code> on this manager's selector and hooks
     * <code>netEventHandler</code> up to receive events whenever the selection occurs.
     */
    public SelectionKey register (SelectableChannel chan, int ops, NetEventHandler netEventHandler)
        throws IOException
    {
        SelectionKey key = chan.register(_selector, ops);
        _handlers.put(key, netEventHandler);
        return key;
    }

    /**
     * Introduces a new active socket into Presents from off the ConnectionManager thread. If
     * Presents is embedded in another framework that handles socket acceptance, this will be
     * called by its socket acceptor to get the socket into Presents to start authorization.
     */
    public void transferAcceptedSocket (SocketChannel channel)
    {
        _acceptedq.append(channel);
    }

    /**
     * Queues a connection up to be closed on the conmgr thread.
     */
    public void closeConnection (Connection conn)
    {
        _deathq.append(conn);
    }

    @Override // from LoopingThread
    protected void willStart ()
    {
        super.willStart();

        _selectorSelector = new SelectorIterable(
            _selector, _selectLoopTime, new SelectorIterable.SelectFailureHandler() {
            public void handleSelectFailure (Exception e) {
                log.error("One of our selectors crapped out completely.  " +
                          "Shutting down the connection manager.", e);
                shutdown();
            }
        });
    }

    @Override // from LoopingThread
    protected void iterate ()
    {
        // performs the select loop; this is the body of the conmgr thread
        final long iterStamp = System.currentTimeMillis();

        // note whether or not we're generating a debug report
        boolean generateDebugReport = (iterStamp - _lastDebugStamp > DEBUG_REPORT_INTERVAL);
        if (DEBUG_REPORT && generateDebugReport) {
            _lastDebugStamp = iterStamp;
        }

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
        long idleStamp = iterStamp - _idleTime;
        for (NetEventHandler handler : _handlers.values()) {
            if (handler.checkIdle(idleStamp)) {
                // this will queue the connection for closure on our next tick
                handler.becameIdle();
            }
        }

        // send any messages that are waiting on the outgoing overflow and message queues
        sendOutgoingMessages(iterStamp);

        // we may be in the middle of shutting down (in which case super.isRunning() is false but
        // isRunning() is true); this is because we stick around until the dobject manager is
        // totally done so that we can send shutdown-related events out to our clients; during
        // those last moments we don't want to accept new connections or read any incoming messages
        if (super.isRunning()) {
            handleIncoming(iterStamp);
        }

        if (DEBUG_REPORT && generateDebugReport) {
            log.info("CONMGR status " + getStats());
        }
    }

    protected void handleIncoming (long iterStamp)
    {
        SocketChannel accepted;
        while ((accepted = _acceptedq.getNonBlocking()) != null) {
            handleAcceptedSocket(accepted);
        }

        // listen for and process incoming network events
        processIncomingEvents(iterStamp);
    }

    /**
     * Adds a connection for the given socket to the managed set.
     */
    protected abstract void handleAcceptedSocket (SocketChannel channel);

    protected void handleAcceptedSocket (SocketChannel channel, Connection conn)
    {
        try {
            // create a new authing connection object to manage the authentication of this client
            // connection and register it with our selection set
            channel.configureBlocking(false);
            conn.init(this, channel, System.currentTimeMillis());
            conn.selkey = register(channel, SelectionKey.OP_READ, conn);
            synchronized (this) {
                _stats.connects++;
            }

        } catch (IOException ioe) {
            // no need to generate a warning because this happens in the normal course of events
            log.info("Failure accepting new connection: " + ioe);
            // make sure we don't leak a socket if something went awry
            try {
                channel.socket().close();
            } catch (IOException ioe2) {
                log.warning("Failed closing aborted connection: " + ioe2);
            }
        }
    }

    /**
     * Checks for any network events on our set of sockets and passes those events down to their
     * associated {@link NetEventHandler}s for processing.
     */
    protected void processIncomingEvents (long iterStamp)
    {
        // process those events
        long bytesIn = 0, msgsIn = 0, eventCount = 0;
        for (SelectionKey selkey : _selectorSelector) {
            eventCount++;
            NetEventHandler handler = null;
            try {
                handler = _handlers.get(selkey);
                if (handler == null) {
                    log.warning("Received network event for unknown handler",
                                "key", selkey, "ops", selkey.readyOps());
                    // request that this key be removed from our selection set, which normally
                    // happens automatically but for some reason didn't
                    selkey.cancel();
                    continue;
                }

//                 log.info("Got event", "selkey", selkey, "handler", handler);

                int got = handler.handleEvent(iterStamp);
                if (got != 0) {
                    bytesIn += got;
                    // we know that the handlers only report having read bytes when they have a
                    // whole message, so we can count thusly
                    msgsIn++;
                }

            } catch (Exception e) {
                log.warning("Error processing network data: " + handler + ".", e);

                // if you freak out here, you go straight in the can
                if (handler != null && handler instanceof Connection) {
                    closeConnection((Connection)handler);
                }
            }
        }

        synchronized (this) {
            // update our stats
            _stats.eventCount += eventCount;
            _stats.bytesIn += bytesIn;
            _stats.msgsIn += msgsIn;
        }
    }

    /**
     * Writes all queued overflow and normal messages to their respective sockets. Connections that
     * already have established overflow queues will have their messages appended to their overflow
     * queue instead so that they are delivered in the proper order.
     */
    protected void sendOutgoingMessages (long iterStamp)
    {
        // first attempt to send any messages waiting on the overflow queues
        if (_oflowqs.size() > 0) {
            // do this on a snapshot as a network failure writing oflow queue messages will result
            // in the queue being removed from _oflowqs via the connectionFailed() code path
            for (OverflowQueue oq : _oflowqs.values().toArray(new OverflowQueue[_oflowqs.size()])) {
                try {
                    // try writing the messages in this overflow queue
                    if (oq.writeOverflowMessages(iterStamp)) {
                        // if they were all written, we can remove it
                        _oflowqs.remove(oq.conn);
                    }

                } catch (IOException ioe) {
                    oq.conn.networkFailure(ioe);
                }
            }
        }

        // then send any new messages
        Tuple<Connection, byte[]> tup;
        while ((tup = _outq.getNonBlocking()) != null) {
            Connection conn = tup.left;

            // if an overflow queue exists for this client, go ahead and slap the message on there
            // because we can't send it until all other messages in their queue have gone out
            OverflowQueue oqueue = _oflowqs.get(conn);
            if (oqueue != null) {
                int size = oqueue.size();
                if ((size > 500) && (size % 50 == 0)) {
                    log.warning("Aiya, big overflow queue for " + conn + "", "size", size,
                                "bytes", tup.right.length);
                }
                oqueue.add(tup.right);
                continue;
            }

            // otherwise write the message out to the client directly
            writeMessage(conn, tup.right, _oflowHandler);
        }
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

        // if this is an asynchronous close request, queue the connection up for death
        if (data == ASYNC_CLOSE_REQUEST) {
            closeConnection(conn);
            return true;
        }

        // sanity check the message size
        if (data.length > 1024 * 1024) {
            log.warning("Refusing to write very large message", "conn", conn, "size", data.length);
            return true;
        }

        // expand our output buffer if needed to accomodate this message
        if (data.length > _outbuf.capacity()) {
            // increase the buffer size in large increments
            int ncapacity = Math.max(_outbuf.capacity() << 1, data.length);
            log.info("Expanding output buffer size", "nsize", ncapacity);
            _outbuf = ByteBuffer.allocateDirect(ncapacity);
        }

        boolean fully = true;
        try {
//             log.info("Writing " + data.length + " byte message to " + conn + ".");

            // first copy the data into our "direct" output buffer
            _outbuf.put(data);
            _outbuf.flip();

            // if the connection to which we're writing is not yet ready, the whole message is
            // "leftover", so we pass it to the partial write handler
            SocketChannel sochan = conn.getChannel();
            if (sochan.isConnectionPending()) {
                pwh.handlePartialWrite(conn, _outbuf);
                return false;
            }

            // then write the data to the socket
            int wrote = sochan.write(_outbuf);
            noteWrite(1, wrote);

            // if we didn't write our entire message, deal with the leftover bytes
            if (_outbuf.remaining() > 0) {
                fully = false;
                pwh.handlePartialWrite(conn, _outbuf);
            }

        } catch (NotYetConnectedException nyce) {
            // this should be caught by isConnectionPending() but awesomely it's not
            pwh.handlePartialWrite(conn, _outbuf);
            return false;

        } catch (IOException ioe) {
            conn.networkFailure(ioe); // instruct the connection to deal with its failure

        } finally {
            _outbuf.clear();
        }

        return fully;
    }

    /** Called by {@link #writeMessage} and friends when they write data over the network. */
    protected synchronized void noteWrite (int msgs, int bytes)
    {
        _stats.msgsOut += msgs;
        _stats.bytesOut += bytes;
    }

    /**
     * Posts a fake message to this connection's outgoing message queue that will cause the
     * connection to be closed when this message is reached. This is only used by outgoing
     * connections to ensure that they finish sending their queued outgoing messages before closing
     * their connection. Incoming connections tend only to be closed at the request of the client
     * or in case of delinquincy. In neither circumstance do we need to flush the client's outgoing
     * queue before closing.
     */
    protected void postAsyncClose (Connection conn)
    {
        _outq.append(Tuple.newTuple(conn, ASYNC_CLOSE_REQUEST));
    }

    /**
     * Called by a connection if it experiences a network failure.
     */
    protected void connectionFailed (Connection conn, IOException ioe)
    {
        // remove this connection from our mappings (it is automatically removed from the Selector
        // when the socket is closed)
        _handlers.remove(conn.selkey);
        _connections.remove(conn.getConnectionId());
        _oflowqs.remove(conn);
        synchronized (this) {
            _stats.disconnects++;
        }
    }

    /**
     * Called by a connection when it discovers that it's closed.
     */
    protected void connectionClosed (Connection conn)
    {
        // remove this connection from our mappings (it is automatically removed from the Selector
        // when the socket is closed)
        _handlers.remove(conn.selkey);
        _connections.remove(conn.getConnectionId());
        _oflowqs.remove(conn);
        synchronized (this) {
            _stats.closes++;
        }
    }

    @Override
    protected void handleIterateFailure (Exception e)
    {
        // log the exception
        log.warning("ConnectionManager.iterate() uncaught exception.", e);
    }

    @Override
    protected void didShutdown ()
    {
        // take one last crack at the outgoing message queue
        sendOutgoingMessages(System.currentTimeMillis());

        // report if there's anything left on the outgoing message queue
        if (_outq.size() > 0) {
            log.warning("Connection Manager failed to deliver " + _outq.size() + " message(s).");
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

    /** Used to handle partial writes in {@link ConnectionManager#writeMessage}. */
    protected static interface PartialWriteHandler
    {
        void handlePartialWrite (Connection conn, ByteBuffer buffer);
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
                // if our outgoing channel is gone or closed, then bail immediately
                SocketChannel sochan = conn.getChannel();
                if (sochan == null || (!sochan.isConnected() && !sochan.isConnectionPending())) {
                    throw new IOException("Connection unavailable for overflow write " + sochan);
                }
                if (sochan.isConnectionPending()) {
                    return false; // not ready to write to this connection yet
                }

                // write all we can of our partial buffer
                int wrote = sochan.write(_partial);
                noteWrite(0, wrote);

                if (_partial.remaining() == 0) {
                    _partial = null;
                    _partials++;
                } else {
//                     log.info("Still going", "conn", conn, "wrote", wrote,
//                              "remain", _partial.remaining());
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
        public void handlePartialWrite (Connection wconn, ByteBuffer buffer)
        {
            // set up our _partial buffer
            _partial = ByteBuffer.allocateDirect(buffer.remaining());
            _partial.put(buffer);
            _partial.flip();
        }

        @Override
        public String toString ()
        {
            return "[conn=" + conn + ", partials=" + _partials + ", msgs=" + _msgs + "]";
        }

        /** The remains of a message that was only partially written on its first attempt. */
        protected ByteBuffer _partial;

        /** A couple of counters. */
        protected int _msgs, _partials;
    }

    /** Used to create an overflow queue on the first partial write. */
    protected PartialWriteHandler _oflowHandler = new PartialWriteHandler() {
        public void handlePartialWrite (Connection conn, ByteBuffer msgbuf) {
            // if we couldn't write all the data for this message, we'll need to establish an
            // overflow queue
            _oflowqs.put(conn, new OverflowQueue(conn, msgbuf));
        }
    };

    protected Selector _selector;
    protected SelectorIterable _selectorSelector;

    /** Maps selection keys to network event handlers. */
    protected Map<SelectionKey, NetEventHandler> _handlers = Maps.newHashMap();

    /** Connections mapped by identifier. */
    protected IntMap<Connection> _connections = IntMaps.newHashIntMap();

    protected Queue<Connection> _deathq = Queue.newQueue();
    protected Queue<SocketChannel> _acceptedq = Queue.newQueue();

    protected Queue<Tuple<Connection, byte[]>> _outq = Queue.newQueue();

    protected ByteBuffer _outbuf = ByteBuffer.allocateDirect(64 * 1024);

    protected Map<Connection, OverflowQueue> _oflowqs = Maps.newHashMap();

    /** Our current runtime stats. */
    protected ConMgrStats _stats = new ConMgrStats();

    /** Used to periodically report connection manager activity when in debug mode. */
    protected long _lastDebugStamp;

    /** A runnable to execute when the connection manager thread exits. */
    protected volatile Runnable _onExit;

    /** Duration in milliseconds for which we wait for network events before checking our running
     * flag to see if we should still be running. We don't want to loop too tightly, but we need to
     * make sure we don't sit around listening for incoming network events too long when there are
     * outgoing messages in the queue. */
    @Inject(optional=true) @Named("presents.net.selectLoopTime")
    protected int _selectLoopTime = 100;

    protected final long _idleTime;

    /** Used to denote asynchronous close requests. */
    protected static final byte[] ASYNC_CLOSE_REQUEST = new byte[0];

    /** Whether or not debug reporting is activated .*/
    protected static final boolean DEBUG_REPORT = false;

    /** Report our activity every 30 seconds. */
    protected static final long DEBUG_REPORT_INTERVAL = 30*1000L;

    /** The number of milliseconds beyond the ping interval that we allow a client's network
     * connection to be idle before we forcibly disconnect them. */
    protected static final long LATENCY_GRACE = 30 * 1000L;
}
