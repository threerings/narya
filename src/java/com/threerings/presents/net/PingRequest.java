//
// $Id: PingRequest.java,v 1.5 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

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

    public short getType ()
    {
        return TYPE;
    }

    public String toString ()
    {
        return "[type=PING, msgid=" + messageId + "]";
    }
}
