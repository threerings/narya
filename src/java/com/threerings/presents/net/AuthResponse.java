//
// $Id: AuthResponse.java,v 1.12 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * The auth response communicates authentication success or failure as
 * well as associated information via a distribted object transmitted
 * along with the response. The distributed object simply serves as a
 * container for the varied and manifold data involved in the
 * authentication process.
 */
public class AuthResponse extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public AuthResponse ()
    {
        super();
    }

    /**
     * Constructs a auth response with the supplied response data.
     */
    public AuthResponse (AuthResponseData data)
    {
        _data = data;
    }

    public AuthResponseData getData ()
    {
        return _data;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeObject(_data);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _data = (AuthResponseData)in.readObject();
    }

    public String toString ()
    {
        return "[type=ARSP, msgid=" + messageId + ", data=" + _data + "]";
    }

    protected AuthResponseData _data;
}
