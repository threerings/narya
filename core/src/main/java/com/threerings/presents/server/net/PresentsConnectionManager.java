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

package com.threerings.presents.server.net;

import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import java.security.PrivateKey;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Invoker;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.Queue;
import com.samskivert.util.ResultListener;
import com.samskivert.util.Tuple;

import com.threerings.io.ByteBufferInputStream;
import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.UnreliableObjectInputStream;
import com.threerings.io.UnreliableObjectOutputStream;

import com.threerings.presents.annotation.AuthInvoker;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.PresentsConMgrStats;
import com.threerings.presents.net.Message;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;
import com.threerings.presents.net.Transport;
import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.ChainedAuthenticator;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.DummyAuthenticator;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.presents.server.ReportManager;
import com.threerings.presents.util.DatagramSequencer;
import com.threerings.presents.util.SecureUtil;

import com.threerings.nio.conman.Connection;
import com.threerings.nio.conman.ConnectionManager;
import com.threerings.nio.conman.NetEventHandler;

import static com.threerings.presents.Log.log;

@Singleton
public class PresentsConnectionManager extends ConnectionManager
    implements ReportManager.Reporter
{

    @Inject
    public PresentsConnectionManager (Lifecycle cycle, ReportManager repmgr)
        throws IOException
    {
        super(cycle, LATENCY_GRACE + PingRequest.PING_INTERVAL);
        repmgr.registerReporter(this);
        _stats = new PresentsConMgrStats();
    }

    @Override
    public synchronized PresentsConMgrStats getStats ()
    {
        ((PresentsConMgrStats)_stats).authQueueSize = _authq.size();
        return ((PresentsConMgrStats)super.getStats());
    }

    // from interface ReportManager.Reporter
    public void appendReport (StringBuilder report, long now, long sinceLast, boolean reset)
    {
        PresentsConMgrStats stats = getStats();
        long eventCount = stats.eventCount - _lastStats.eventCount;
        int connects = stats.connects - _lastStats.connects;
        int disconnects = stats.disconnects - _lastStats.disconnects;
        int closes = stats.closes - _lastStats.closes;
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
        report.append(stats.connectionCount).append(" connections, ");
        report.append(stats.handlerCount).append(" handlers\n");
        report.append("- Network activity: ");
        report.append(eventCount).append(" events, ");
        report.append(connects).append(" connects, ");
        report.append(disconnects).append(" disconnects, ");
        report.append(closes).append(" closes\n");
        report.append("- Network input: ");
        report.append(bytesIn).append(" bytes, ");
        report.append(msgsIn).append(" msgs, ");
        report.append(msgsIn*1000/sinceLast).append(" mps, ");
        long avgIn = (msgsIn == 0) ? 0 : (bytesIn/msgsIn);
        report.append(avgIn).append(" avg size, ");
        report.append(bytesIn*1000/sinceLast).append(" bps\n");
        report.append("- Network output: ");
        report.append(bytesOut).append(" bytes, ");
        report.append(msgsOut).append(" msgs, ");
        report.append(msgsOut*1000/sinceLast).append(" mps, ");
        long avgOut = (msgsOut == 0) ? 0 : (bytesOut/msgsOut);
        report.append(avgOut).append(" avg size, ");
        report.append(bytesOut*1000/sinceLast).append(" bps\n");
    }

    /**
     * Adds an authenticator to the authentication chain. This authenticator will be offered a
     * chance to authenticate incoming connections before falling back to the main authenticator.
     */
    public void addChainedAuthenticator (ChainedAuthenticator author)
    {
        _authors.add(author);
    }

    /**
     * Sets the private key if the ciphers are supported.
     *
     * @return true if the key is set
     */
    public boolean setPrivateKey (PrivateKey key)
    {
        if (SecureUtil.ciphersSupported(key)) {
            _privateKey = key;
            return true;
        }
        return false;
    }

    /**
     * Sets the private key if the ciphers are supported.
     *
     * @return true if the key is set
     */
    public boolean setPrivateKey (String key)
    {
        return (key != null) && setPrivateKey(SecureUtil.stringToRSAPrivateKey(key));
    }

    /**
     * Returns the private key used in secure authentication.
     */
    public PrivateKey getPrivateKey ()
    {
        return _privateKey;
    }

    /**
     * Called when a datagram message is ready to be read off its channel.
     */
    protected int handleDatagram (DatagramChannel listener, long when)
    {
        InetSocketAddress source;
        _databuf.clear();
        try {
            source = (InetSocketAddress)listener.receive(_databuf);
        } catch (IOException ioe) {
            log.warning("Failure receiving datagram.", ioe);
            return 0;
        }

        // make sure we actually read a packet
        if (source == null) {
            log.info("Psych! Got READ_READY, but no datagram.");
            return 0;
        }

        // flip the buffer and record the size (which must be at least 14 to contain the connection
        // id, authentication hash, and a class reference)
        int size = _databuf.flip().remaining();
        if (size < 14) {
            log.warning("Received undersized datagram", "source", source, "size", size);
            return 0;
        }

        // the first four bytes are the connection id
        int connectionId = _databuf.getInt();
        Connection conn = _connections.get(connectionId);
        if (conn != null) {
            ((PresentsConnection)conn).handleDatagram(source, listener, _databuf, when);
        } else {
            log.debug("Received datagram for unknown connection", "id", connectionId,
                      "source", source);
        }

        return size;
    }

    /**
     * Called by a connection when it has a downstream message that needs to be delivered.
     * <em>Note:</em> this method is called as a result of a call to
     * {@link PresentsConnection#postMessage} which happens when forwarding an event to a client
     * and at the completion of authentication, both of which <em>must</em> happen only on the
     * distributed object thread.
     */
    protected void postMessage (PresentsConnection conn, Message msg)
    {
        if (!isRunning()) {
            log.warning("Posting message to inactive connection manager",
                        "msg", msg, new Exception());
        }

        // sanity check
        if (conn == null || msg == null) {
            log.warning("postMessage() bogosity", "conn", conn, "msg", msg, new Exception());
            return;
        }

        // more sanity check; messages must only be posted from the dobjmgr thread
        if (!_omgr.isDispatchThread()) {
            log.warning("Message posted on non-distributed object thread", "conn", conn,
                        "msg", msg, "thread", Thread.currentThread(), new Exception());
            // let it through though as we don't want to break things unnecessarily
        }

        try {
            // send it as a datagram if hinted and possible (pongs must be sent as part of the
            // negotation process)
            if (!msg.getTransport().isReliable() &&
                    (conn.getTransmitDatagrams() || msg instanceof PongResponse) &&
                        postDatagram(conn, msg)) {
                return;
            }

            // note the actual transport
            msg.noteActualTransport(Transport.RELIABLE_ORDERED);

            _framer.resetFrame();

            // flatten this message using the connection's output stream
            ObjectOutputStream oout = conn.getObjectOutputStream(_framer);
            oout.writeObject(msg);
            oout.flush();

            // now extract that data into a byte array
            ByteBuffer buffer = _framer.frameAndReturnBuffer();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            // log.info("Flattened " + msg + " into " + data.length + " bytes.");

            // and slap both on the queue
            _outq.append(Tuple.<Connection, byte[]>newTuple(conn, data));

        } catch (Exception e) {
            log.warning("Failure flattening message", "conn", conn, "msg", msg, e);
        }
    }

    /**
     * Helper function for {@link #postMessage}; handles posting the message as a datagram.
     *
     * @return true if the datagram was successfully posted, false if it was too big.
     */
    protected boolean postDatagram (PresentsConnection conn, Message msg)
        throws Exception
    {
        _flattener.reset();

        // flatten the message using the connection's sequencer
        DatagramSequencer sequencer = conn.getDatagramSequencer();
        sequencer.writeDatagram(msg);

        // if the message is too big, we must fall back to sending it through the stream channel
        if (_flattener.size() > Client.MAX_DATAGRAM_SIZE) {
            return false;
        }

        // note the actual transport
        msg.noteActualTransport(Transport.UNRELIABLE_UNORDERED);

        // extract as a byte array
        byte[] data = _flattener.toByteArray();

        // slap it on the queue
        _dataq.append(Tuple.newTuple(conn, data));

        return true;
    }

    /**
     * Creates a datagram sequencer for use by a {@link Connection}.
     */
    protected DatagramSequencer createDatagramSequencer ()
    {
        return new DatagramSequencer(
            new UnreliableObjectInputStream(new ByteBufferInputStream(_databuf)),
            new UnreliableObjectOutputStream(_flattener));
    }

    /**
     * Opens an outgoing connection to the supplied address. The connection will be opened in a
     * non-blocking manner and added to the connection manager's select set. Messages posted to the
     * connection prior to it being actually connected to its destination will remain in the queue.
     * If the connection fails those messages will be dropped.
     *
     * @param conn the connection to be initialized and opened. Callers may want to provide a
     * {@link Connection} derived class so that they may intercept calldown methods.
     * @param hostname the hostname of the server to which to connect.
     * @param port the port on which to connect to the server.
     *
     * @exception IOException thrown if an error occurs creating our socket. Everything else
     * happens asynchronously. If the connection attempt fails, the Connection will be notified via
     * {@link Connection#networkFailure}.
     */
    public void openOutgoingConnection (Connection conn, String hostname, int port)
        throws IOException
    {
        // create a socket channel to use for this connection, initialize it and queue it up to
        // have the non-blocking connect process started
        SocketChannel sockchan = SocketChannel.open();
        sockchan.configureBlocking(false);
        conn.init(this, sockchan, System.currentTimeMillis());
        _connectq.append(Tuple.newTuple(conn, new InetSocketAddress(hostname, port)));
    }

    /**
     * Starts the connection process for an outgoing connection. This is called as part of the
     * conmgr tick for any pending outgoing connections.
     */
    protected void startOutgoingConnection (final Connection conn, InetSocketAddress addr)
    {
        final SocketChannel sockchan = conn.getChannel();
        try {
            // register our channel with the selector (if this fails, we abandon ship immediately)
            conn.selkey = sockchan.register(_selector, SelectionKey.OP_CONNECT);

            // start our connection process (now if we fail we need to clean things up)
            NetEventHandler handler;
            if (sockchan.connect(addr)) {
                // it is possible even for a non-blocking socket to connect immediately, in which
                // case we stick the connection in as its event handler immediately
                handler = conn;

            } else {
                // otherwise we wire up a special event handler that will wait for our socket to
                // finish the connection process and then wire things up fully
                handler = new OutgoingConnectionHandler(conn);
            }
            _handlers.put(conn.selkey, handler);

        } catch (IOException ioe) {
            log.warning("Failed to initiate connection for " + sockchan + ".", ioe);
            conn.connectFailure(ioe); // nothing else to clean up
        }
    }

    @Override // from LoopingThread
    protected void iterate ()
    {
        super.iterate();

        // reap any outgoing connection handlers that failed to connect due to idleness
        OutgoingConnectionHandler handler;
        while ((handler = _outfailq.getNonBlocking()) != null) {
            handler.handleError(new IOException("Pending connection became idle."));
        }
    }

    @Override // from LoopingThread
    public boolean isRunning ()
    {
        // Prevent exiting our thread until the object manager is done.
        return super.isRunning() || _omgr.isRunning();
    }

    @Override
    protected void handleIncoming (long iterStamp)
    {
        super.handleIncoming(iterStamp);

        // start up any outgoing connections that need to be connected
        Tuple<Connection, InetSocketAddress> pconn;
        while ((pconn = _connectq.getNonBlocking()) != null) {
            startOutgoingConnection(pconn.left, pconn.right);
        }

        // check for connections that have completed authentication
        processAuthedConnections(iterStamp);
    }

    @Override
    protected void connectionFailed (Connection conn, IOException ioe)
    {
        super.connectionFailed(conn, ioe);

        // let the client manager know what's up
        _clmgr.connectionFailed(conn, ioe);
    }

    @Override
    protected void connectionClosed (Connection conn)
    {
        super.connectionClosed(conn);

        // let the client manager know what's up
        _clmgr.connectionClosed(conn);
    }

    /**
     * Performs the authentication process on the specified connection. This is called by {@link
     * AuthingConnection} itself once it receives its auth request.
     */
    protected void authenticateConnection (AuthingConnection conn)
    {
        Authenticator author = _author;
        for (ChainedAuthenticator cauthor : _authors) {
            if (cauthor.shouldHandleConnection(conn)) {
                author = cauthor;
                break;
            }
        }

        author.authenticateConnection(_authInvoker, conn, new ResultListener<AuthingConnection>() {
            public void requestCompleted (AuthingConnection conn) {
                _authq.append(conn);
            }
            public void requestFailed (Exception cause) {
                // this never happens
            }
        });
    }

    /**
     * Starts an accepted socket down the path to authorization.
     */
    @Override
    protected void handleAcceptedSocket (SocketChannel channel)
    {
        handleAcceptedSocket(channel, new AuthingConnection());
    }

    /**
     * Converts connections that have completed the authentication process into full running
     * connections and notifies the client manager that new connections have been established.
     */
    protected void processAuthedConnections (long iterStamp)
    {
        AuthingConnection conn;
        while ((conn = _authq.getNonBlocking()) != null) {
            try {
                // construct a new running connection to handle this connections network traffic
                // from here on out
                PresentsConnection rconn = new PresentsConnection();
                rconn.init(this, conn.getChannel(), iterStamp);
                rconn.selkey = conn.selkey;

                // we need to keep using the same object input and output streams from the
                // beginning of the session because they have context that needs to be preserved
                rconn.inheritStreams(conn);

                // replace the mapping in the handlers table from the old conn with the new one
                _handlers.put(rconn.selkey, rconn);

                // add a mapping for the connection id and set the datagram secret
                _connections.put(rconn.getConnectionId(), rconn);
                rconn.setDatagramSecret(conn.getAuthRequest().getCredentials().getDatagramSecret());

                // transfer any overflow queue for that connection
                OverflowQueue oflowHandler = _oflowqs.remove(conn);
                if (oflowHandler != null) {
                    _oflowqs.put(rconn, oflowHandler);
                }

                // and let the client manager know about our new connection
                _clmgr.connectionEstablished(rconn, conn.getAuthName(), conn.getAuthRequest(),
                                             conn.getAuthResponse());

            } catch (IOException ioe) {
                log.warning("Failure upgrading authing connection to running.", ioe);
            }
        }
    }

    @Override
    protected void sendOutgoingMessages (long iterStamp)
    {
        super.sendOutgoingMessages(iterStamp);

        // send any datagrams
        Tuple<PresentsConnection, byte[]> tup;
        while ((tup = _dataq.getNonBlocking()) != null) {
            writeDatagram(tup.left, tup.right);
        }
    }

    /**
     * Sends a datagram to the specified connection.
     *
     * @return true if the datagram was sent, false if we failed to send for any reason.
     */
    protected boolean writeDatagram (PresentsConnection conn, byte[] data)
    {
        InetSocketAddress target = conn.getDatagramAddress();
        if (target == null) {
            log.warning("No address to send datagram", "conn", conn);
            return false;
        }

        _databuf.clear();
        _databuf.put(data).flip();
        try {
            return conn.getDatagramChannel().send(_databuf, target) > 0;
        } catch (IOException ioe) {
            log.warning("Failed to send datagram.", ioe);
            return false;
        }
    }

    protected class OutgoingConnectionHandler implements NetEventHandler
    {
        public OutgoingConnectionHandler (Connection conn)
        {
            _conn = conn;
        }

        public int handleEvent (long when)
        {
            SocketChannel sockchan = _conn.getChannel();
            try {
                if (sockchan.finishConnect()) {
                    // great, we're ready to roll, wire up the connection
                    _conn.selkey = sockchan.register(_selector, SelectionKey.OP_READ);
                    _handlers.put(_conn.selkey, _conn);
                    log.info("Outgoing connection ready", "conn", _conn);
                }
            } catch (IOException ioe) {
                handleError(ioe);
            }
            return 0;
        }

        public boolean checkIdle (long idleStamp)
        {
            return _conn.checkIdle(idleStamp);
        }

        public void becameIdle ()
        {
            // this failed connection will be cleaned up in the next iterate() tick
            _outfailq.append(this);
        }

        protected void handleError (IOException ioe)
        {
            _handlers.remove(_conn.selkey);
            _oflowqs.remove(_conn);
            _conn.connectFailure(ioe);
        }

        protected final Connection _conn;
    }

    /** Handles client authentication. The base authenticator is injected but optional services
     * like the PeerManager may replace this authenticator with one that intercepts certain types
     * of authentication and then passes normal authentications through. */
    @Inject(optional=true) protected Authenticator _author = new DummyAuthenticator();
    protected List<ChainedAuthenticator> _authors = Lists.newArrayList();
    protected PrivateKey _privateKey;

    protected Queue<AuthingConnection> _authq = Queue.newQueue();
    protected Queue<Tuple<Connection, InetSocketAddress>> _connectq = Queue.newQueue();

    /** failed (idled out) outgoing connections that need to be cleaned up */
    protected Queue<OutgoingConnectionHandler> _outfailq = Queue.newQueue();

    protected FramingOutputStream _framer = new FramingOutputStream();
    protected ByteArrayOutputStream _flattener = new ByteArrayOutputStream();

    // some dependencies
    @Inject @AuthInvoker protected Invoker _authInvoker;
    @Inject protected ClientManager _clmgr;
    @Inject protected PresentsDObjectMgr _omgr;

    /** A snapshot of our runtime stats as of our last report. */
    protected PresentsConMgrStats _lastStats = new PresentsConMgrStats();

    protected Queue<Tuple<PresentsConnection, byte[]>> _dataq = Queue.newQueue();
    protected ByteBuffer _databuf = ByteBuffer.allocateDirect(Client.MAX_DATAGRAM_SIZE);
}
