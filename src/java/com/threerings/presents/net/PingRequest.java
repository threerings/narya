//
// $Id: PingRequest.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.net;

public class PingNotification extends UpstreamMessage
{
    /** The code for a ping notification. */
    public static final short TYPE = TYPE_BASE + 5;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public PingNotification ()
    {
        super();
    }

    public short getType ()
    {
        return TYPE;
    }
}
