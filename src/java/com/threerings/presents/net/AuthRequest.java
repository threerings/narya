//
// $Id: AuthRequest.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.cocktail.cher.io.TypedObjectFactory;

public class AuthenticationRequest extends UpstreamMessage
{
    /** The code for an object subscription request. */
    public static final short TYPE = TYPE_BASE + 0;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public AuthenticationRequest ()
    {
        super();
    }

    /**
     * Constructs a authentication request with the supplied credentials.
     */
    public AuthenticationRequest (Credentials creds)
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
        _creds.writeTo(out);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _creds = (Credentials)TypedObjectFactory.readFrom(in);
    }

    /**
     * The object id of the distributed object to which we are
     * subscribing.
     */
    protected Credentials _creds;
}
