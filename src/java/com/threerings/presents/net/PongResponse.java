//
// $Id: PongResponse.java,v 1.6 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

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

    public String toString ()
    {
        return "[type=PONG, msgid=" + messageId + "]";
    }
}
