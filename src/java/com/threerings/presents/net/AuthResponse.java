//
// $Id: AuthResponse.java,v 1.13 2002/12/20 23:28:24 mdb Exp $

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

    public String toString ()
    {
        return "[type=ARSP, msgid=" + messageId + ", data=" + _data + "]";
    }

    protected AuthResponseData _data;
}
