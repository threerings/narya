//
// $Id: AuthRequest.java,v 1.3 2001/05/23 04:03:40 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.cocktail.cher.io.TypedObjectFactory;

public class AuthRequest extends UpstreamMessage
{
    /** The code for an auth request. */
    public static final short TYPE = TYPE_BASE + 0;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public AuthRequest ()
    {
        super();
    }

    /**
     * Constructs a auth request with the supplied credentials.
     */
    public AuthRequest (Credentials creds)
    {
        _creds = creds;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        TypedObjectFactory.writeTo(out, _creds);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _creds = (Credentials)TypedObjectFactory.readFrom(in);
    }

    /**
     * The credentials associated with this auth request.
     */
    protected Credentials _creds;
}
