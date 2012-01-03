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

import java.util.List;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.samskivert.net.AddressUtil;
import com.samskivert.util.Lifecycle;

import static com.threerings.NaryaLog.log;

/**
 * Binds sockets on a given hostname for a set of ports for tcp connections, and passes accepted
 * connections into a connection manager.
 */
public class ServerSocketChannelAcceptor
    implements Lifecycle.ShutdownComponent
{
    /**
     * Configures the connection manager with the hostname and ports on which it will listen for
     * socket connections. This must be called before the connection manager is started as the
     * sockets will be bound at that time.
     *
     * @param socketHostname the hostname to which we bind our sockets or null to bind to all
     * interfaces.
     * @param socketPorts the ports on which to listen for TCP connection.
     */
    public ServerSocketChannelAcceptor (String socketHostname, int[] socketPorts,
            ConnectionManager mgr)
    {
        Preconditions.checkNotNull(socketPorts, "Socket ports must be non-null.");
        _bindHostname = socketHostname;
        _ports = socketPorts;
        _conMan = mgr;
    }

    /**
     * Bind to the socket ports and return true if any of the binds succeeded.
     */
    public boolean bind ()
    {
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
        return successes > 0;
    }

    /**
     * Unbind our listening sockets.
     */
    public void shutdown ()
    {
        for (ServerSocketChannel ssocket : _ssockets) {
            try {
                ssocket.socket().close();
            } catch (IOException ioe) {
                log.warning("Failed to close listening socket: " + ssocket, ioe);
            }
        }
    }

    protected void acceptConnections (int port)
        throws IOException
    {
        // create a listening socket
        final ServerSocketChannel ssocket = ServerSocketChannel.open();
        configureSocket(ssocket);
        InetSocketAddress isa = AddressUtil.getAddress(_bindHostname, port);
        ssocket.socket().bind(isa);

        _conMan.register(ssocket, SelectionKey.OP_ACCEPT, new NetEventHandler() {
            public int handleEvent (long when) {
                try {
                    _conMan.handleAcceptedSocket(ssocket.accept());
                } catch (IOException ioe) {
                    log.info("Failure accepting connected socket: " +ioe);
                }
                // there's no easy way to measure bytes read when accepting a connection, so we
                // claim nothing
                return 0;
            }
            public boolean checkIdle (long idleStamp) {
                return false; // we're never idle
            }
            public void becameIdle () {
                // we're never idle
            }
        });
        _ssockets.add(ssocket);
        log.info("Server listening on " + isa + ".");
    }

    /**
     * Override to perform any desired additional socket configuration before binding.  Be sure to
     * call the superclass implementation.
     */
    protected void configureSocket (ServerSocketChannel ssocket)
        throws IOException
    {
        ssocket.configureBlocking(false);
    }

    protected final int[] _ports;
    protected final String _bindHostname;
    protected final ConnectionManager _conMan;

    protected List<ServerSocketChannel> _ssockets = Lists.newArrayList();
}
