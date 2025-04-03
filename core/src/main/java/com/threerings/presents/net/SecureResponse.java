//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.security.PrivateKey;

import com.samskivert.util.StringUtil;

import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.util.SecureUtil;

/**
 * Used to indicate a authentication response based on a SecureRequest.
 */
public class SecureResponse extends AuthResponse
    implements AuthCodes
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public SecureResponse ()
    {
        super();
    }

    /**
     * Creates a secure response with the response code.
     */
    public SecureResponse (String code)
    {
        _data = new AuthResponseData();
        _data.code = code;
    }

    /**
     * Encodes the server secret in the response data, or sets the failed state.
     *
     * @return the server secret if successfully encoded, or null.
     */
    public byte[] createSecret (PublicKeyCredentials pkcred, PrivateKey key, int length)
    {
        _data = new AuthResponseData();
        byte[] clientSecret = pkcred.getSecret(key);
        if (clientSecret == null) {
            _data.code = FAILED_TO_SECURE;
            return null;
        }
        byte[] secret = SecureUtil.createRandomKey(length);
        _data.code = StringUtil.hexlate(SecureUtil.xorBytes(secret, clientSecret));
        return secret;
    }

    /**
     * Returns the code bytes or null for a failed state.
     */
    public byte[] getCodeBytes (PublicKeyCredentials pkcreds)
    {
        return pkcreds == null || _data.code == null || _data.code.equals(FAILED_TO_SECURE) ?
            null : SecureUtil.xorBytes(StringUtil.unhexlate(_data.code), pkcreds.getSecret());
    }
}
