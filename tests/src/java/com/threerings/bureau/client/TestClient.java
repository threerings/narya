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

import com.samskivert.util.OneLineLogFormatter;
import com.threerings.bureau.Log;
import com.samskivert.util.Queue;
import com.samskivert.util.StringUtil;
import com.threerings.bureau.client.BureauDirector;
import com.threerings.bureau.data.AgentObject;
import com.samskivert.util.RunQueue;

/**
 * Extends bureau client minimally and provides a static main function to create a client and 
 * connect to a server given by system properties.
 */
public class TestClient extends BureauClient
{
    public static void main (String args[])
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
        client.run();
    }

    /**
     * Implements most basic run queue. Required to instantate a client. 
     */
    static protected class SimpleRunQueue implements RunQueue
    {
        public void postRunnable (Runnable r)
        {
            _queue.append(r);
        }

        public boolean isDispatchThread ()
        {
            return _main == Thread.currentThread();
        }

        public void run ()
        {
            _main = Thread.currentThread();

            while (true) {
                Runnable r = _queue.get();
                r.run();
            }
        }

        protected Thread _main;
        protected Queue<Runnable> _queue = new Queue<Runnable>();
    }

    /**
     * The agent class used by our director. Does not actually load any code, just logs the 
     * start/stop requests.
     */
    static protected class TestAgent extends Agent
    {
        public void start ()
        {
            Log.info("Starting agent " + StringUtil.toString(_agentObj));
        }

        public void stop ()
        {
            Log.info("Stopping agent " + StringUtil.toString(_agentObj));
        }
    }

    /**
     * Constructs a new test client.
     */
    protected TestClient (String token, String bureauId)
    {
        super(token, bureauId, new SimpleRunQueue());
    }

    /**
     * Runs the event loop.
     */
    protected void run ()
    {
        ((SimpleRunQueue)_runQueue).run();
    }

    // overridden - creates a simple director
    protected BureauDirector createDirector ()
    {
        // just use our test agent exclusively - in the real world, the agent created would depend 
        // on the object's type and/or properties
        return new BureauDirector(_ctx) {
            public Agent createAgent (AgentObject agentObj) {
                return new TestAgent();
            }
        };
    }

}

