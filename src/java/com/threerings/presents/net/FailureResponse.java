//
// $Id: FailureResponse.java,v 1.1 2001/05/22 21:51:29 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class FailureResponse extends DownstreamMessage
{
    /** The code for a logoff notification. */
    public static final short TYPE = TYPE_BASE + 3;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public FailureResponse ()
    {
        super();
    }

    /**
     * Constructs a failure response that is associated with the specified
     * upstream message id.
     */
    public FailureResponse (short messageId)
    {
        this.messageId = messageId;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeShort(messageId);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        messageId = in.readShort();
    }
}
