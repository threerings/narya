//
// $Id: PongResponse.java,v 1.3 2001/06/05 21:53:45 mdb Exp $

package com.threerings.cocktail.cher.net;

public class PongResponse extends DownstreamMessage
{
    /** The code for a pong response. */
    public static final short TYPE = TYPE_BASE + 4;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public PongResponse ()
    {
        super();
    }

    public short getType ()
    {
        return TYPE;
    }
}
