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

package com.threerings.crowd.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.SessionFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsSession;
import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.chat.server.ChatProvider;

/**
 * Extends the Presents server configuring extensions for Crowd services.
 */
@Singleton
public class CrowdServer extends PresentsServer
{
    /** Configures dependencies needed by the Crowd services. */
    public static class CrowdModule extends PresentsModule
    {
        @Override protected void configure () {
            super.configure();
            // nada (yet)
        }
    }

    /**
     * Initializes all of the server services and prepares for operation.
     */
    @Override
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client manager to use our bits
        _clmgr.setDefaultSessionFactory(new SessionFactory() {
            @Override
            public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
                return CrowdSession.class;
            }
            @Override
            public Class<? extends ClientResolver> getClientResolverClass (Name username) {
                return CrowdClientResolver.class;
            }
        });
    }

    public static void main (String[] args)
    {
        runServer(new CrowdModule(), new PresentsServerModule(CrowdServer.class));
    }

    /** Handles the creation and tracking of place managers. */
    @Inject protected PlaceRegistry _plreg;

    /** Handles body-related invocation services. */
    @Inject protected BodyManager _bodyman;

    /** Handles location-related invocation services. */
    @Inject protected LocationManager _locman;

    /** Provides chat-related invocation services. */
    @Inject protected ChatProvider _chatprov;

    /** The config key for our list of invocation provider mappings. */
    protected final static String PROVIDERS_KEY = "providers";
}
