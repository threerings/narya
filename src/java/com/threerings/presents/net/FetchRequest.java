//
// $Id: FetchRequest.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class FetchRequest extends UpstreamMessage
{
    /** The code for an object fetch request. */
    public static final short TYPE = TYPE_BASE + 2;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public FetchRequest ()
    {
        super();
    }

    /**
     * Constructs a fetch request for the distributed object with the
     * specified object id.
     */
    public FetchRequest (int oid)
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
     * The object id of the distributed object which we are fetching.
     */
    protected int _oid;
}
