//
// $Id: FailureResponse.java,v 1.3 2001/06/02 01:30:37 mdb Exp $

package com.threerings.cocktail.cher.net;

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
     * Constructs a failure response in response to a request for the
     * specified oid.
     */
    public FailureResponse (int oid)
    {
        _oid = oid;
    }

    public short getType ()
    {
        return TYPE;
    }

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

    protected int _oid;
}
