//
// $Id: AuthResponse.java,v 1.10 2001/12/03 22:01:57 mdb Exp $

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

    /**
     * The authenticator can stuff information into the auth response that
     * will be made available to the client management code on the server
     * after the authentication process has completed. This is provided
     * because the authenticator will likely end up doing things like
     * loading information from a database which it will want to pass
     * along to the running system once authentication is complete.
     */
    public void setAuthInfo (Object authInfo)
    {
        _authInfo = authInfo;
    }

    /**
     * Returns the auth info provided by the authenticator implementation.
     */
    public Object getAuthInfo ()
    {
        return _authInfo;
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

    /** Stores auth info provided by the authenticator. */
    protected Object _authInfo;
}
