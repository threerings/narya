//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
