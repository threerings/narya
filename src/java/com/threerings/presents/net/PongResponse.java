//
// $Id: PongResponse.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.net;

public class PongNotification extends DownstreamMessage
{
    /** The code for a pong notification. */
    public static final short TYPE = TYPE_BASE + 4;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public PongNotification ()
    {
        super();
    }

    public short getType ()
    {
        return TYPE;
    }
}
