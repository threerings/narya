//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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
        Injector injector = Guice.createInjector(new Module());
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
