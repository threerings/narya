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

import com.samskivert.util.OneLineLogFormatter;
import com.google.common.collect.Lists;
import java.util.Collections;
import com.threerings.bureau.data.AgentObject;
import java.util.List;
import java.util.Random;

/**
 * Uses a TestServer to pound on the BureauRegistry. Sends random sequences of
 * startAgent and destroyAgent. Most aspects of the randomness are configurable using system 
 * properties.
 * TODO: add more types of testing
 * TODO: allow a quitAfter configuration parameter to shut down automatically
 */
public class RegistryTester
{
    /**
     * Convenience function for getting an integral property. Also logs the value.
     */
    public static int intProp (String name, int defaultVal)
    {
        String val = System.getProperty(name);
        if (val == null) {
            TestServer.log.info("Property " + name + " is " + defaultVal);
            return defaultVal;
        }
        int ival = Integer.parseInt(val);
        TestServer.log.info("Property " + name + " is " + ival);
        return ival;
    }

    /**
     * Creates a new server and runs the registry tester on it.
     */
    public static void main (String[] args)
    {
        // make log pretty
        OneLineLogFormatter.configureDefaultHandler();

        TestServer server = new TestServer();
        RegistryTester tester = new RegistryTester(server);

        try {
            server.init();
            tester.start();
            server.run();

        } catch (Exception e) {
            TestServer.log.warning("Unable to initialize server.");
            TestServer.logStackTrace(e);
        }
    }

    /**
     * Creates a new registry tester.
     */
    public RegistryTester (TestServer server)
    {
        _server = server;

        _maxAgents = intProp("maxAgents", 500);
        _numBureaus = intProp("numBureaus", 5);
        _maxOps = intProp("maxOps", 5);
        _createChance = intProp("createChance", 70);
        _minDelay = intProp("minDelay", 500);
        _maxDelay = intProp("maxDelay", 2000);

        // stop the tests when the server shuts down
        // TODO: this is not called on Ctrl-C, need a way to shut down gracefully
        _server.registerShutdowner(new TestServer.Shutdowner() {
            public void shutdown () {
                TestServer.log.info("Shutting down tests");
                _stop = true;
            }
        });
    }

    /**
     * Starts the test thread.
     */
    public void start ()
    {
        Thread thread = new Thread("Registry test thread") {
            public void run () {
                TestServer.log.info(getName() + " started");
                runTestThread();
                TestServer.log.info(getName() + " stopped");
            }
        };
        thread.start();
    }

    /**
     * Stops the test thread.
     */
    public void stop ()
    {
        _stop = true;
    }
    
    /**
     * The main loop for the tests.
     */
    protected void runTestThread ()
    {
        // set up the rngs (2 are needed since one is called from the dobj thread)
        long seed = Long.parseLong(System.getProperty("seed", "0"));
        if (seed == 0) {
            seed = System.currentTimeMillis();
        }
        _rng1 = new Random(seed);
        _rng2 = new Random(seed);

        TestServer.log.info("Running tests, seed is " + seed);

        // runnable that generates N requests to create or destroy agents
        Runnable createOrDestroyAgents = new Runnable() {
            public void run () {
                int ops = 1 + _rng1.nextInt(_maxOps);
                TestServer.log.info("Starting " + ops + " agent requests");
                for (int i = 0; i < ops; ++i) {
                    randomlyCreateOrDestroyAgent();
                }
                TestServer.log.info("Finished " + ops + " agent requests");
            }
        };

        // the main test loop
        while (!_stop) {
            try {
                // sleep for a bit
                int sleep = _maxDelay - _minDelay + 1;
                sleep = _minDelay + _rng2.nextInt(sleep);
                Thread.sleep(sleep);
            }
            catch (InterruptedException ie) {
                break;
            }

            // create or destroy some agents
            _server.omgr.postRunnable(createOrDestroyAgents);
        }

        // clean up
        _server.omgr.postRunnable(new Runnable() {
            public void run () {
                for (AgentObject obj : _agents) {
                    _server.breg.destroyAgent(obj);
                }
            }
        });
    }

    /**
     * Does what the name says using the configuration values.
     */
    protected void randomlyCreateOrDestroyAgent ()
    {
        int size = _agents.size();
        if (size >= _maxAgents ||
            (size != 0 && _rng1.nextInt(100) >= _createChance)) {
            AgentObject toRemove = _agents.remove(_rng1.nextInt(size));
            TestServer.log.info("Removing agent " + toRemove.getOid());
            _server.breg.destroyAgent(toRemove);
        }
        else {
            AgentObject added = create(_rng1.nextInt(_numBureaus) + 1);
            _agents.add(added);
            TestServer.log.info("Added agent " + added.getOid());
        }
    }

    /**
     * Create and return a new agent for the given bureau.
     */
    protected AgentObject create (int bureau)
    {
        AgentObject obj = new AgentObject();
        obj.bureauType = "test";
        obj.bureauId = "test-" + bureau;
        _server.breg.startAgent(obj);
        return obj;
    }

    protected TestServer _server;
    protected boolean _stop;
    protected Random _rng1;
    protected Random _rng2;
    protected List<AgentObject> _agents = Lists.newArrayList();

    // maximum number of agents to keep around
    protected int _maxAgents;
    
    // number of bureaus to select between
    protected int _numBureaus;

    // number of operations to perform in one dobj queue task
    protected int _maxOps;

    // chance of creating a new agent if limit has not been reached
    protected int _createChance;

    // minimum delay between batches of requests
    protected int _minDelay;

    // maximum delay between batches of requests
    protected int _maxDelay;
}
