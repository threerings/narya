//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * The auth response communicates authentication success or failure as
 * well as associated information via a distribted object transmitted
 * along with the response. The distributed object simply serves as a
 * container for the varied and manifold data involved in the
 * authentication process.
 */
public class AuthResponse extends DownstreamMessage
{
    /** Auxilliary authentication data to be communicated to the <code>
     * PresentsSession</code> once a session is started. This is a means by
     * which the <code>Authenticator</code> can pass information loaded from,
     * say, an authentication database into the runtime system to be used, for
     * example for permissions. */
    public transient Object authdata;

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
     * Replaces this response's auth data.
     */
    public void setData (AuthResponseData data)
    {
        _data = data;
    }

    @Override
    public String toString ()
    {
        return "[type=ARSP, msgid=" + messageId + ", data=" + _data + "]";
    }

    protected AuthResponseData _data;
}
