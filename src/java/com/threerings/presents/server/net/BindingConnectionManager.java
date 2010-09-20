//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import java.util.List;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.net.AddressUtil;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.StringUtil;

import com.threerings.presents.server.ReportManager;

import static com.threerings.presents.Log.log;

/**
 * Binds tcp sockets and listens for datagrams in addition to ConnectionManager's normal duties.
 */
@Singleton
public class BindingConnectionManager extends ConnectionManager
{
    @Inject public BindingConnectionManager (Lifecycle cycle, ReportManager repmgr)
        throws IOException
    {
        super(cycle, repmgr);
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
    public void init (String socketHostname, String datagramHostname,
                      int[] socketPorts, int[] datagramPorts)
        throws IOException
    {
        Preconditions.checkNotNull(socketPorts, "Socket ports must be non-null.");
        Preconditions.checkNotNull(datagramPorts, "Datagram ports must be non-null. " +
                                   "Pass a zero-length array to bind no datagram ports.");

        _bindHostname = socketHostname;
        _ports = socketPorts;
        _datagramHostname = datagramHostname;
        _datagramPorts = datagramPorts;
    }

    @Override
    protected void willStart ()
    {
        super.willStart();

        // open our TCP listening ports
        int successes = 0;
        for (int port : _ports) {
            try {
                acceptConnections(port);
                successes++;
            } catch (IOException ioe) {
                log.warning("Failure listening to socket", "hostname", _bindHostname,
                            "port", port, ioe);
            }
        }
        if (successes == 0) {
            log.warning("ConnectionManager failed to bind to any ports. Shutting down.");
            _server.queueShutdown();
            return;
        }

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

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();
        // TODO: consider closing the listen sockets earlier, like in the shutdown method

        // unbind our listening sockets; note: because we wait for the objmgr to exit before we do,
        // we will still be accepting connections as long as there are events pending.
        for (ServerSocketChannel ssocket : _ssockets) {
            try {
                ssocket.socket().close();
            } catch (IOException ioe) {
                log.warning("Failed to close listening socket: " + ssocket, ioe);
            }
        }

        // unbind datagram channels, if any
        for (DatagramChannel datagramChannel : _datagramChannels) {
            datagramChannel.socket().close();
        }
    }

    protected void acceptConnections (int port)
        throws IOException
    {
        // create a listening socket
        final ServerSocketChannel ssocket = ServerSocketChannel.open();
        ssocket.configureBlocking(false);
        InetSocketAddress isa = AddressUtil.getAddress(_bindHostname, port);
        ssocket.socket().bind(isa);

        // and add it to the select set
        SelectionKey sk = ssocket.register(_selector, SelectionKey.OP_ACCEPT);
        _handlers.put(sk, new NetEventHandler() {
            public int handleEvent (long when) {
                try {
                    handleAcceptedSocket(ssocket.accept());
                } catch (IOException ioe) {
                    log.info("Failure accepting connected socket: " +ioe);
                }
                // there's no easy way to measure bytes read when accepting a connection, so we
                // claim nothing
                return 0;
            }
            public boolean checkIdle (long now) {
                return false; // we're never idle
            }
            public void becameIdle () {
                // we're never idle
            }
        });
        _ssockets.add(ssocket);
        log.info("Server listening on " + isa + ".");
    }

    protected void acceptDatagrams (int port)
        throws IOException
    {
        // create a channel
        final DatagramChannel channel = DatagramChannel.open();
        channel.socket().setTrafficClass(0x10); // IPTOS_LOWDELAY
        channel.configureBlocking(false);
        InetSocketAddress isa = AddressUtil.getAddress(_datagramHostname, port);
        channel.socket().bind(isa);

        // and add it to the select set
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

    protected int[] _ports, _datagramPorts;
    protected String _bindHostname, _datagramHostname;

    protected List<ServerSocketChannel> _ssockets = Lists.newArrayList();
    protected List<DatagramChannel> _datagramChannels = Lists.newArrayList();
}
