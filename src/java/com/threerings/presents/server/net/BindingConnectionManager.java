package com.threerings.presents.server.net;

import java.util.List;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;

import com.threerings.presents.server.ReportManager;

import com.threerings.nio.SocketChannelAcceptor;

import static com.threerings.presents.Log.log;

/**
 * Binds tcp sockets and listens for datagrams in addition to ConnectionManager's normal duties.
 */
@Singleton
public class BindingConnectionManager extends ConnectionManager
{
    @Inject public BindingConnectionManager (Lifecycle cycle, ReportManager repmgr,
        IncomingEventWaitHolder incomingEventWait)
        throws IOException
    {
        super(cycle, repmgr, incomingEventWait);
    }

    /**
     * Configures the connection manager with the hostname and ports on which it will listen for
     * socket connections and datagram packets. This must be called before the connection manager
     * is started (via {@via #start}) as the sockets will be bound at that time.
     *
     * @param socketHostname the hostname to which we bind our sockets or null to bind to all
     * interfaces.
     * @param datagramHostname the hostname to which we bind our datagram socket or null to bind
     * to all interfaces.
     * @param socketPorts the ports on which to listen for TCP connection.
     * @param datagramPorts the ports on which to listen for datagram packets.
     */
    public void init (String socketHostname, String datagramHostname, int[] socketPorts,
        int[] datagramPorts)
        throws IOException
    {
        Preconditions.checkNotNull(socketPorts, "Socket ports must be non-null.");
        Preconditions.checkNotNull(datagramPorts, "Datagram ports must be non-null. " +
                                    "Pass a zero-length array to bind no datagram ports.");

        // Listen for socket connections, but don't wait to select on them.  The connection check
        // occurs as part of ConnectionManager's incoming event loop, which already has a wait.
        _socketAcceptor = new SocketChannelAcceptor(this, _failureHandler, socketHostname,
            socketPorts, 0);

        _datagramPorts = datagramPorts;
        _datagramHostname = datagramHostname;
    }

    @Override
    protected void willStart ()
    {
        if (!_socketAcceptor.listen()) {
            log.warning("ConnectionManager failed to bind to any ports. Shutting down.");
            _server.queueShutdown();
        } else {
            // open up the datagram ports as well
            for (int port : _datagramPorts) {
                try {
                    acceptDatagrams(port);
                } catch (IOException ioe) {
                    log.warning("Failure opening datagram channel", "hostname", _datagramHostname,
                                "port", port, ioe);
                }
            }
        }
    }

    @Override
    protected void processIncomingEvents (long iterStamp)
    {
        _socketAcceptor.tick(iterStamp);
        super.processIncomingEvents(iterStamp);
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();

        // TODO: consider closing the listen sockets earlier, like in the shutdown method
        _socketAcceptor.shutdown();
        // unbind datagram channels, if any
        for (DatagramChannel datagramChannel : _datagramChannels) {
            datagramChannel.socket().close();
        }
    }

    protected void acceptDatagrams (int port)
        throws IOException
    {
        // create a channel and add it to the select set
        final DatagramChannel channel = DatagramChannel.open();
        channel.socket().setTrafficClass(0x10); // IPTOS_LOWDELAY
        channel.configureBlocking(false);
        InetSocketAddress isa = SocketChannelAcceptor.getAddress(_datagramHostname, port);
        channel.socket().bind(isa);
        SelectionKey sk = channel.register(_selector, SelectionKey.OP_READ);
        _handlers.put(sk, new NetEventHandler() {
            public int handleEvent (long when) {
                return handleDatagram(channel, when);
            }

            public boolean checkIdle (long now) {
                return false;// Can't be idle
            }

            public void becameIdle () {}
        });
        _datagramChannels.add(channel);
        log.info("Server accepting datagrams on " + isa + ".");
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
            conn.handleDatagram(source, listener, _databuf, when);
        } else {
            log.debug("Received datagram for unknown connection", "id", connectionId,
                      "source", source);
        }

        return size;
    }

    protected SocketChannelAcceptor _socketAcceptor;
    protected int[] _datagramPorts;
    protected String _datagramHostname;

    protected final List<DatagramChannel> _datagramChannels = Lists.newArrayList();
}