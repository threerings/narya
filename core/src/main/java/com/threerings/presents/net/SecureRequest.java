//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.security.PrivateKey;

/**
 * Used to create a secure channel to the server.
 */
public class SecureRequest extends AuthRequest
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public SecureRequest ()
    {
        super();
    }

    /**
     * Constructs a auth request with the supplied credentials and client version information.
     */
    public SecureRequest (PublicKeyCredentials creds, String version)
    {
        super(creds, version, new String[0]);
    }

    /**
     * Returns the secret from the credentials.
     */
    public byte[] getSecret (PrivateKey key)
    {
        return ((PublicKeyCredentials)_creds).getSecret(key);
    }
}
