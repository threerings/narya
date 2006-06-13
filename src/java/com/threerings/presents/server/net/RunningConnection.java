//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.threerings.presents.net.UpstreamMessage;

/**
 * A running connection object represents a fully operational client
 * connection to the server.
 */
public class RunningConnection extends Connection
{
    /**
     * Constructs a new running connection object to manage the supplied
     * client socket.
     */
    public RunningConnection (ConnectionManager cmgr, SelectionKey selkey,
                              SocketChannel channel, long createStamp)
        throws IOException
    {
        super(cmgr, selkey, channel, createStamp);
    }

    /**
     * Called when a new message has arrived from the client.
     */
    public void handleMessage (UpstreamMessage msg)
    {
    }

    public String toString ()
    {
        return "[mode=RUNNING, id=" + (hashCode() % 1000) +
            ", addr=" + getInetAddress() + "]";
    }
}
