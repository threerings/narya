//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Let's the server know we're still alive.
 */
public class PingRequest extends UpstreamMessage
{
    /** The number of milliseconds of idle upstream that are allowed to elapse before the client
     * sends a ping message to the server to let it know that we're still alive. */
    public static final long PING_INTERVAL = 60 * 1000L;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public PingRequest ()
    {
        super();
    }

    /**
     * Creates a new ping request using the specified transport.
     */
    public PingRequest (Transport transport)
    {
        _transport = transport;
    }

    /**
     * Returns a timestamp that was obtained when this packet was encoded by the low-level
     * networking code.
     */
    public long getPackStamp ()
    {
        return _packStamp;
    }

    /**
     * Returns a timestamp that was obtained when this packet was decoded by the low-level
     * networking code.
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
        // grab a timestamp noting when we were encoded into a raw buffer for delivery over the
        // network
        _packStamp = System.currentTimeMillis();

        out.defaultWriteObject();
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        // grab a timestamp noting when we were decoded from a raw buffer after being received over
        // the network
        _unpackStamp = System.currentTimeMillis();

        in.defaultReadObject();
    }

    @Override
    public void setTransport (Transport transport)
    {
        _transport = transport;
    }

    @Override
    public Transport getTransport ()
    {
        return _transport;
    }

    @Override
    public String toString ()
    {
        return "[type=PING, msgid=" + messageId + ", transport=" + _transport + "]";
    }

    /** A time stamp obtained when we serialize this object. */
    protected transient long _packStamp;

    /** A time stamp obtained when we unserialize this object (the intent is to get a timestamp as
     * close as possible to when the packet was received on the network). */
    protected transient long _unpackStamp;

    /** The transport parameters. */
    protected transient Transport _transport = Transport.DEFAULT;
}
