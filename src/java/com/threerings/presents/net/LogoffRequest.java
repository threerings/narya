//
// $Id: LogoffRequest.java,v 1.5 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

public class LogoffRequest extends UpstreamMessage
{
    /** The code for a logoff request. */
    public static final short TYPE = TYPE_BASE + 6;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public LogoffRequest ()
    {
        super();
    }

    public short getType ()
    {
        return TYPE;
    }

    public String toString ()
    {
        return "[type=LOGOFF, msgid=" + messageId + "]";
    }
}
