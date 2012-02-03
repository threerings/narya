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

package com.threerings.presents.server;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.Credentials;

/**
 * Used to determine what type of {@link PresentsSession} to use to manage an authenticated client
 * as well the type of {@link ClientResolver} to use when resolving clients' runtime data.
 */
public abstract class SessionFactory
{
    /** The default client factory. */
    public static SessionFactory DEFAULT = new SessionFactory() {
        @Override public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
            return PresentsSession.class;
        }
        @Override public Class<? extends ClientResolver> getClientResolverClass (Name username) {
            return ClientResolver.class;
        }
    };

    /**
     * Creates a session factory that handles clients with the supplied credentials and
     * authentication name.
     */
    public static SessionFactory newSessionFactory (
        final Class<? extends Credentials> credsClass,
        final Class<? extends PresentsSession> sessionClass,
        final Class<? extends Name> nameClass,
        final Class<? extends ClientResolver> resolverClass)
    {
        return new SessionFactory() {
            @Override public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
                return credsClass.isInstance(areq.getCredentials()) ? sessionClass : null;
            }
            @Override
            public Class <? extends ClientResolver> getClientResolverClass (Name username) {
                return nameClass.isInstance(username) ? resolverClass : null;
            }
        };
    }

    /**
     * Returns the {@link PresentsSession} derived class to use for the session that authenticated
     * with the supplied request or null if this factory does not handle sessions of the supplied
     * type.
     */
    public abstract Class<? extends PresentsSession> getSessionClass (AuthRequest areq);

    /**
     * Returns the {@link ClientResolver} derived class to use to resolve a client with the
     * specified username or null if this factory does not handle clients of the supplied type.
     */
    public abstract Class <? extends ClientResolver> getClientResolverClass (Name username);
}
