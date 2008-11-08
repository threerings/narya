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

package com.threerings.bureau.client;

import com.samskivert.util.BasicRunQueue;
import com.samskivert.util.OneLineLogFormatter;
import com.samskivert.util.StringUtil;

import com.threerings.bureau.data.AgentObject;

import static com.threerings.bureau.Log.log;

/**
 * Extends bureau client minimally and provides a static main function to create a client and 
 * connect to a server given by system properties.
 */
public class TestClient extends BureauClient
{
    public static void main (String[] args)
        throws java.net.MalformedURLException
    {
        // make log pretty
        OneLineLogFormatter.configureDefaultHandler();

        // create the client and log on
        TestClient client = new TestClient(
            System.getProperty("token"), 
            System.getProperty("bureauId"));
        client.setServer(
            System.getProperty("serverName"), 
            new int[] {Integer.parseInt(System.getProperty("serverPort"))});
        client.logon();

        // run it
        ((BasicRunQueue)client.getRunQueue()).run();
    }

    /**
     * The agent class used by our director. Does not actually load any code, just logs the
     * start/stop requests.
     */
    protected static class TestAgent extends Agent
    {
        @Override public void start () {
            log.info("Starting agent " + StringUtil.toString(_agentObj));
        }

        @Override public void stop () {
            log.info("Stopping agent " + StringUtil.toString(_agentObj));
        }
    }

    /**
     * Constructs a new test client.
     */
    protected TestClient (String token, String bureauId)
    {
        super(token, bureauId, new BasicRunQueue());
    }

    // overridden - creates a simple director
    @Override
    protected BureauDirector createDirector ()
    {
        // just use our test agent exclusively - in the real world, the agent created would depend 
        // on the object's type and/or properties
        return new BureauDirector(_ctx) {
            @Override public Agent createAgent (AgentObject agentObj) {
                return new TestAgent();
            }
        };
    }
}
