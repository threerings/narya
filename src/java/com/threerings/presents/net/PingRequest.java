//
// $Id: PingRequest.java,v 1.6 2002/05/28 21:56:38 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PingRequest extends UpstreamMessage
{
    /** The code for a ping request. */
    public static final short TYPE = TYPE_BASE + 5;

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

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);

        // grab a timestamp noting when we were encoded into a raw buffer
        // for delivery over the network
        _packStamp = System.currentTimeMillis();
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);

        // grab a timestamp noting when we were decoded from a raw buffer
        // after being received over the network
        _unpackStamp = System.currentTimeMillis();
    }

    public short getType ()
    {
        return TYPE;
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
