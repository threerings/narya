//
// $Id: PingRequest.java,v 1.3 2001/06/05 21:29:51 mdb Exp $

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
}
