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
