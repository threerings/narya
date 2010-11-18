//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.net {

import com.adobe.crypto.MD5;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.Joiner;

/**
 * Extends the basic credentials to provide bureau-specific fields.
 */
public class ServiceCreds extends Credentials
{
    /** The id of the service client that is authenticating. */
    public var clientId :String;

    /**
     * Creates credentials for the specified client.
     */
    public function ServiceCreds (clientId :String = null, sharedSecret :String = null)
    {
        this.clientId = clientId;
        _authToken = createAuthToken(clientId, sharedSecret);
    }

    /**
     * Validates that these credentials were created with the supplied shared secret.
     */
    public function areValid (sharedSecret :String) :Boolean
    {
        return createAuthToken(clientId, sharedSecret) == _authToken;
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(clientId);
        out.writeField(_authToken);
    }

    // from Object
    override protected function toStringJoiner (j :Joiner) :void
    {
        super.toStringJoiner(j);
        j.add("token", _authToken);
    }

    /**
     * Creates a unique password for the specified node using the supplied shared secret.
     */
    protected static function createAuthToken (clientId :String, sharedSecret :String) :String
    {
        return MD5.hash(clientId + sharedSecret);
    }

    /** A token created by a call to {@link #createAuthToken}. */
    protected var _authToken :String;
}
}
