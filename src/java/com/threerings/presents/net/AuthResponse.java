//
// $Id: AuthResponse.java,v 1.9 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.dobj.io.DObjectFactory;

/**
 * The auth response communicates authentication success or failure as
 * well as associated information via a distribted object transmitted
 * along with the response. The distributed object simply serves as a
 * container for the varied and manifold data involved in the
 * authentication process.
 */
public class AuthResponse extends DownstreamMessage
{
    /** The code for an auth response. */
    public static final short TYPE = TYPE_BASE + 0;

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

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        DObjectFactory.writeTo(out, _data);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _data = (AuthResponseData)DObjectFactory.readFrom(in);
    }

    public String toString ()
    {
        return "[type=ARSP, msgid=" + messageId + ", data=" + _data + "]";
    }

    protected AuthResponseData _data;
}
