//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.bureau.Log;
import com.threerings.bureau.data.AgentObject;
import com.threerings.presents.server.PresentsServer;
import com.samskivert.util.OneLineLogFormatter;
import com.samskivert.util.StringUtil;

import java.io.File;

/**
 * Extends a presents server to include a bureau registry.
 */
public class TestServer extends PresentsServer
{
    /**
     * The bureau registry for the server. Will be null until <code>init</code> is called.
     */
    public static BureauRegistry breg;

    /**
     * Creates a new server and runs it.
     */
    public static void main (String[] args)
    {
        // make log pretty
        OneLineLogFormatter.configureDefaultHandler();

        final TestServer server = new TestServer();
        try {
            server.init();

            // request a new agent
            // TODO: more tests here - create a thread that posts different kinds of requests
            // and somehow monitors results from client
            server.omgr.postRunnable(new Runnable() { 
                public void run () {
                    server.createTestAgent();
                }
            });

            server.run();

        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    // inherit documentation - from PresentsServer
    public void init ()
        throws Exception
    {
        super.init();
        breg = new BureauRegistry("localhost:47624", invmgr, omgr);

        breg.setCommandGenerator("test", new BureauRegistry.CommandGenerator() {
            public String[] createCommand (
                String serverNameAndPort, 
                String bureauId, 
                String token) {
                
                int colon = serverNameAndPort.indexOf(':');
                String [] cmd = {"ant", 
                    "-DserverName=" + serverNameAndPort.substring(0, colon),
                    "-DserverPort=" + serverNameAndPort.substring(colon + 1),
                    "-DbureauId=" + bureauId,
                    "-Dtoken=" + token,
                    "bureau-runclient"};

                return cmd;
            }
        });
    }

    /**
     * Requests that the bureau client create an agent.
     */
    protected void createTestAgent ()
    {
        AgentObject obj = new AgentObject();
        obj.bureauType = "test";
        obj.bureauId = "test";
        breg.startAgent(obj);
    }
}
