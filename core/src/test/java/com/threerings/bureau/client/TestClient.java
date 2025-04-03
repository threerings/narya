//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
