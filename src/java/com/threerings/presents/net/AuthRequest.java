//
// $Id: AuthRequest.java,v 1.8 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthRequest extends UpstreamMessage
{
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

    public Credentials getCredentials ()
    {
        return _creds;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeObject(_creds);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _creds = (Credentials)in.readObject();
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
