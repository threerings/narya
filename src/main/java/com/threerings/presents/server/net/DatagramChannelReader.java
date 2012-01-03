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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.samskivert.net.AddressUtil;
import com.samskivert.util.Lifecycle;

import com.threerings.nio.conman.NetEventHandler;

import static com.threerings.presents.Log.log;

/**
 * Binds datagram connections on a given hostname for a set of ports, and passes datagrams read
 * off those ports into a connection manager.
 */
public class DatagramChannelReader
    implements Lifecycle.ShutdownComponent
{
    public DatagramChannelReader (String datagramHostname,
            int[] datagramPorts, PresentsConnectionManager conMgr)
    {
        Preconditions.checkNotNull(datagramPorts, "Datagram ports must be non-null. " +
                                   "Pass a zero-length array to bind no datagram ports.");

        _datagramHostname = datagramHostname;
        _datagramPorts = datagramPorts;
        _conMan = conMgr;
    }

    public void bind()
    {
        for (int port : _datagramPorts) {
            try {
                acceptDatagrams(port);
            } catch (IOException ioe) {
                log.warning("Failure opening datagram channel", "hostname", _datagramHostname,
                            "port", port, ioe);
            }
        }
    }

    public void shutdown ()
    {
        for (DatagramChannel datagramChannel : _datagramChannels) {
            datagramChannel.socket().close();
        }
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
        _conMan.register(channel, SelectionKey.OP_READ, new NetEventHandler() {
            public int handleEvent (long when) {
                return _conMan.handleDatagram(channel, when);
            }
            public boolean checkIdle (long idleStamp) {
                return false; // we can't be idle
            }
            public void becameIdle () {}
        });
        _datagramChannels.add(channel);
        log.info("Server accepting datagrams on " + isa + ".");
    }

    protected final int[] _datagramPorts;
    protected final String _datagramHostname;
    protected final List<DatagramChannel> _datagramChannels = Lists.newArrayList();
    protected final PresentsConnectionManager _conMan;
}
