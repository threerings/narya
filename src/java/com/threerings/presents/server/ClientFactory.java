//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.presents.net.AuthRequest;

/**
 * Used to determine what type of {@link PresentsClient} to use to manage an authenticated client
 * as well the type of {@link ClientResolver} to use when resolving clients' runtime data.
 */
public interface ClientFactory
{
    /** The default client factory. */
    public static ClientFactory DEFAULT = new ClientFactory () {
        public Class<? extends PresentsClient> getClientClass (AuthRequest areq) {
            return PresentsClient.class;
        }
        public Class<? extends ClientResolver> getClientResolverClass (Name username) {
            return ClientResolver.class;
        }
    };

    /**
     * Returns the {@link PresentsClient} derived class to use for the session that authenticated
     * with the supplied request.
     */
    public Class<? extends PresentsClient> getClientClass (AuthRequest areq);

    /**
     * Returns the {@link ClientResolver} derived class to use to resolve a client with the
     * specified username.
     */
    public Class <? extends ClientResolver> getClientResolverClass (Name username);
}
