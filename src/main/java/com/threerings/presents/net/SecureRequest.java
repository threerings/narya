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
