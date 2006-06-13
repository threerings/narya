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

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class PingRequest extends UpstreamMessage
{
    /** The number of milliseconds of idle upstream that are allowed to
     * elapse before the client sends a ping message to the server to let
     * it know that we're still alive. */
    public static final long PING_INTERVAL = 60 * 1000L;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public PingRequest ()
    {
        super();
    }

    /**
     * Returns a timestamp that was obtained when this packet was encoded
     * by the low-level networking code.
     */
    public long getPackStamp ()
    {
        return _packStamp;
    }

    /**
     * Returns a timestamp that was obtained when this packet was decoded
     * by the low-level networking code.
     */
    public long getUnpackStamp ()
    {
        return _unpackStamp;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        // grab a timestamp noting when we were encoded into a raw buffer
        // for delivery over the network
        _packStamp = System.currentTimeMillis();

        out.defaultWriteObject();
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        // grab a timestamp noting when we were decoded from a raw buffer
        // after being received over the network
        _unpackStamp = System.currentTimeMillis();

        in.defaultReadObject();
    }

    public String toString ()
    {
        return "[type=PING, msgid=" + messageId + "]";
    }

    /** A time stamp obtained when we serialize this object. */
    protected transient long _packStamp;

    /** A time stamp obtained when we unserialize this object (the intent
     * is to get a timestamp as close as possible to when the packet was
     * received on the network). */
    protected transient long _unpackStamp;
}
