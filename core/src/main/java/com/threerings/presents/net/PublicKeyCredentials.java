//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.threerings.presents.util.SecureUtil;

/**
 * Credentials based on a public key encrypted secret.
 */
public class PublicKeyCredentials extends Credentials
{
    /**
     * No-arg constructor.
     */
    public PublicKeyCredentials ()
    {
    }

    /**
     * Create a public key credential.
     */
    public PublicKeyCredentials (PublicKey key)
    {
        _secret = SecureUtil.createRandomKey(16);
        _salt = SecureUtil.createRandomKey(4);
        _encodedSecret = SecureUtil.encryptBytes(key, _secret, _salt);
        _secureVersion = SecureUtil.VERSION;
    }

    /**
     * Returns the secure version the client is using.
     */
    public int getSecureVersion ()
    {
        return _secureVersion;
    }

    /**
     * Returns the secret.
     */
    public byte[] getSecret ()
    {
        return _secret;
    }

    /**
     * Decodes the secret.
     */
    public byte[] getSecret (PrivateKey key)
    {
        if (_secret == null) {
            _secret = SecureUtil.decryptBytes(key, _encodedSecret, _salt);
        }
        return _secret;
    }

    @Override // documentation inherited
    public String getDatagramSecret ()
    {
        return new String(_encodedSecret);
    }

    /** Our transmitted key. */
    protected byte[] _encodedSecret;

    /** Our verification salt. */
    protected byte[] _salt;

    /** Our secure version. */
    protected int _secureVersion;

    /** Our secret key. */
    protected transient byte[] _secret;
}
