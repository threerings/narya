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

import com.samskivert.util.StringUtil;

/**
 * Credentials used by service clients (peers, bureaus, etc.). A service would extend this class
 * (so that their service clients can be identified by class name).
 */
public abstract class ServiceCreds extends Credentials
{
    /** The id of the service client that is authenticating. */
    public String clientId;

    /**
     * Creates credentials for the specified client.
     */
    public ServiceCreds (String clientId, String sharedSecret)
    {
        this.clientId = clientId;
        _authToken = createAuthToken(clientId, sharedSecret);
    }

    /**
     * Used when unserializing an instance from the network.
     */
    public ServiceCreds ()
    {
    }

    /**
     * Validates that these credentials were created with the supplied shared secret.
     */
    public boolean areValid (String sharedSecret)
    {
        return createAuthToken(clientId, sharedSecret).equals(_authToken);
    }

    @Override // from Object
    public String toString ()
    {
        return getClass().getSimpleName() + "[id=" + clientId + ", token=" + _authToken + "]";
    }

    /**
     * Creates a unique password for the specified node using the supplied shared secret.
     */
    protected static String createAuthToken (String clientId, String sharedSecret)
    {
        return StringUtil.md5hex(clientId + sharedSecret);
    }

    /** A token created by a call to {@link #createAuthToken}. */
    protected String _authToken;
}
