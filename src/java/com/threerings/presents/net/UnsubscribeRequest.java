//
// $Id: UnsubscribeRequest.java,v 1.1 2001/05/22 06:08:00 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UnsubscribeNotification extends UpstreamMessage
{
    /** The code for an unsubscribe notification. */
    public static final short TYPE = TYPE_BASE + 3;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UnsubscribeNotification ()
    {
        super();
    }

    /**
     * Constructs a unsubscribe notification for the distributed object
     * with the specified object id.
     */
    public UnsubscribeNotification (int oid)
    {
        _oid = oid;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeInt(_oid);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _oid = in.readInt();
    }

    /**
     * The object id of the distributed object from which we are
     * unsubscribing.
     */
    protected int _oid;
}
