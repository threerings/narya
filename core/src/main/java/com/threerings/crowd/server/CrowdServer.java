//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
