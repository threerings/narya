//
// $Id: PongResponse.java,v 1.1 2001/05/22 21:51:29 mdb Exp $

package com.samskivert.cocktail.cher.net;

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
