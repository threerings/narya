//
// $Id: LogoffRequest.java,v 1.6 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

public class LogoffRequest extends UpstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public LogoffRequest ()
    {
        super();
    }

    public String toString ()
    {
        return "[type=LOGOFF, msgid=" + messageId + "]";
    }
}
