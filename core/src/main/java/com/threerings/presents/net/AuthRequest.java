//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.util.TimeZone;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;

/**
 * Used to authenticate with the server.
 */
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
     * Constructs a auth request with the supplied credentials and client version information.
     */
    public AuthRequest (Credentials creds, String version, String[] bootGroups)
    {
        _creds = creds;
        _version = version;
        _zone = TimeZone.getDefault().getID();
        _bootGroups = bootGroups;
    }

    /**
     * Returns a reference to the credentials provided with this request.
     */
    public Credentials getCredentials ()
    {
        return _creds;
    }

    /**
     * Returns a reference to the version information provided with this request.
     */
    public String getVersion ()
    {
        return _version;
    }

    /**
     * Returns the timezone in which this client is operating.
     */
    public TimeZone getTimeZone ()
    {
        return TimeZone.getTimeZone(_zone);
    }

    /**
     * Returns the set of bootstrap service groups in which this client is interested.
     */
    public String[] getBootGroups ()
    {
        return _bootGroups;
    }

    /**
     * Returns a shared secret key used for sending encrypted data to the client.
     */
    public byte[] getSecret ()
    {
        return null;
    }

    @Override
    public String toString ()
    {
        return "[type=AREQ, msgid=" + messageId + ", creds=" + _creds +
            ", version=" + _version + "]";
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        try {
            in.defaultReadObject();
        } catch (IOException ioe) {
            // if we fail here because the client is old, leave ourselves with a partially
            // initialized set of credentials, which the server will generally cope with by telling
            // the client it is out of date
        }
    }

    /** The credentials associated with this auth request. */
    protected Credentials _creds;

    /** The version information associated with the client code. */
    protected String _version;

    /** The timezone in which this client is operating. */
    protected String _zone;

    /** The set of bootstrap service groups this client is interested in. */
    protected String[] _bootGroups;
}
