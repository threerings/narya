//
// $Id: PongResponse.java,v 1.4 2001/06/09 23:39:04 mdb Exp $

package com.threerings.cocktail.cher.net;

public class PongResponse extends DownstreamMessage
{
    /** The code for a pong response. */
    public static final short TYPE = TYPE_BASE + 5;

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
