//
// $Id: PingRequest.java,v 1.7 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class PingRequest extends UpstreamMessage
{
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
        super.writeObject(out);

        // grab a timestamp noting when we were encoded into a raw buffer
        // for delivery over the network
        _packStamp = System.currentTimeMillis();
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);

        // grab a timestamp noting when we were decoded from a raw buffer
        // after being received over the network
        _unpackStamp = System.currentTimeMillis();
    }

    public String toString ()
    {
        return "[type=PING, msgid=" + messageId + "]";
    }

    /** A time stamp obtained when we serialize this object. */
    protected long _packStamp;

    /** A time stamp obtained when we unserialize this object (the intent
     * is to get a timestamp as close as possible to when the packet was
     * received on the network). */
    protected long _unpackStamp;
}
