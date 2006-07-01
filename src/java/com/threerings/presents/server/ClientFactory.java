//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.presents.server;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.net.AuthRequest;

/**
 * Used to create a {@link PresentsClient} instance to manage an authenticated
 * client.
 */
public interface ClientFactory
{
    /** The default client factory. */
    public static ClientFactory DEFAULT = new ClientFactory () {
        public PresentsClient createClient (AuthRequest areq) {
            return new PresentsClient();
        }
        public ClientResolver createClientResolver (Name username) {
            return new ClientResolver();
        }
    };

    /**
     * Creates an uninitialized client instance for the client that has
     * authenticated using the supplied request.
     */
    public PresentsClient createClient (AuthRequest areq);

    /**
     * Requests a resolver for the client identified by the specified username.
     */
    public ClientResolver createClientResolver (Name username);
}
