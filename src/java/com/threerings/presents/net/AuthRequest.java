//
// $Id: AuthRequest.java,v 1.11 2002/12/20 23:41:26 mdb Exp $

package com.threerings.presents.net;

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
     * Constructs a auth request with the supplied credentials and client
     * version information.
     */
    public AuthRequest (Credentials creds, String version)
    {
        _creds = creds;
        _version = version;
    }

    /**
     * Returns a reference to the credentials provided with this request.
     */
    public Credentials getCredentials ()
    {
        return _creds;
    }

    /**
     * Returns a reference to the version information provided with this
     * request.
     */
    public String getVersion ()
    {
        return _version;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[type=AREQ, msgid=" + messageId + ", creds=" + _creds +
            ", version=" + _version + "]";
    }

    /** The credentials associated with this auth request. */
    protected Credentials _creds;

    /** The version information associated with the client code. */
    protected String _version;
}
