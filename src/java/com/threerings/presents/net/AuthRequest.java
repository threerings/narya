//
// $Id: AuthRequest.java,v 1.7 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.io.TypedObjectFactory;

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
