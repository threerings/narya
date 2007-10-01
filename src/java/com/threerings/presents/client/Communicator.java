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

package com.threerings.presents.client;

import com.threerings.presents.net.UpstreamMessage;

/**
 * Handles sending and receiving messages for the client.
 */
public abstract class Communicator
{
    /**
     * Creates a new communicator instance which is associated with the supplied client.
     */
    public Communicator (Client client)
    {
        _client = client;
    }

    /**
     * Logs on to the server and initiates our full-duplex message exchange.
     */
    public abstract void logon ();

    /**
     * Delivers a logoff notification to the server and shuts down the network connection. Also
     * causes all communication threads to terminate.
     */
    public abstract void logoff ();

    /**
     * Queues up the specified message for delivery upstream.
     */
    public abstract void postMessage (UpstreamMessage msg);

    /**
     * Configures this communicator with a custom class loader to be used when reading and writing
     * objects over the network.
     */
    public abstract void setClassLoader (ClassLoader loader);

    /**
     * Returns the time at which we last sent a packet to the server.
     */
    public abstract long getLastWrite ();

    protected Client _client;
}
