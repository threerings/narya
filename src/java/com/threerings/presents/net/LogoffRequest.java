//
// $Id: LogoffRequest.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.net;

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
