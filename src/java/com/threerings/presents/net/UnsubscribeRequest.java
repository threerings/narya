//
// $Id: UnsubscribeRequest.java,v 1.6 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UnsubscribeRequest extends UpstreamMessage
{
    /** The code for an unsubscribe request. */
    public static final short TYPE = TYPE_BASE + 3;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UnsubscribeRequest ()
    {
        super();
    }

    /**
     * Constructs a unsubscribe request for the distributed object
     * with the specified object id.
     */
    public UnsubscribeRequest (int oid)
    {
        _oid = oid;
    }

    public short getType ()
    {
        return TYPE;
    }

    /**
     * Returns the oid of the object from which we are unsubscribing.
     */
    public int getOid ()
    {
        return _oid;
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

    public String toString ()
    {
        return "[type=UNSUB, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    /**
     * The object id of the distributed object from which we are
     * unsubscribing.
     */
    protected int _oid;
}
