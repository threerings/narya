//
// $Id: LogoffRequest.java,v 1.1 2001/05/22 06:08:00 mdb Exp $

package com.samskivert.cocktail.cher.net;

public class LogoffNotification extends UpstreamMessage
{
    /** The code for a logoff notification. */
    public static final short TYPE = TYPE_BASE + 6;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public LogoffNotification ()
    {
        super();
    }

    public short getType ()
    {
        return TYPE;
    }
}
