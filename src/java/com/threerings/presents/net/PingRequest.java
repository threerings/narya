//
// $Id: PingRequest.java,v 1.4 2001/07/19 19:30:14 mdb Exp $

package com.threerings.cocktail.cher.net;

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
