//
// $Id: AuthRequest.java,v 1.6 2001/07/19 19:30:14 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.io.TypedObjectFactory;

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

    public Credentials getCredentials ()
    {
        return _creds;
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

    public String toString ()
    {
        return "[type=AREQ, msgid=" + messageId + ", creds=" + _creds + "]";
    }

    /**
     * The credentials associated with this auth request.
     */
    protected Credentials _creds;
}
