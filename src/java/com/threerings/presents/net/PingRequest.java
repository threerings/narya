//
// $Id: PingRequest.java,v 1.1 2001/05/22 06:08:00 mdb Exp $

package com.samskivert.cocktail.cher.net;

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
