//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.server;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.threerings.presents.server.PresentsServer;

import static com.threerings.bureau.Log.log;

/**
 * Extends a presents server to include a bureau registry.
 */
@Singleton
public class TestServer extends PresentsServer
{
    /**
     * Creates a new server and runs it.
     */
    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new PresentsModule());
        TestServer server = injector.getInstance(TestServer.class);
        try {
            server.init(injector);
            server.setClientTarget("bureau-runclient");
            server.run();

        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
        }
    }

    public static BureauRegistry.CommandGenerator antCommandGenerator (final String target)
    {
        return new BureauRegistry.CommandGenerator() {
            public String[] createCommand (String bureauId, String token) {
                return new String[] {
                    "ant",
                    "-DserverName=localhost",
                    "-DserverPort=47624",
                    "-DbureauId=" + bureauId,
                    "-Dtoken=" + token,
                    target };
            }
        };
    }

    public void setClientTarget (String target)
    {
        _bureauReg.setCommandGenerator("test", antCommandGenerator(target));
    }

    @Inject protected BureauRegistry _bureauReg;
}
