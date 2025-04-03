//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.server;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;

import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.PresentsSession;

import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.data.BureauCredentials;

import static com.threerings.bureau.Log.log;

/**
 * Uses a TestServer to pound on the BureauRegistry. Sends random sequences of
 * startAgent and destroyAgent. Most aspects of the randomness are configurable using system
 * properties.
 * TODO: add more types of testing
 * TODO: allow a quitAfter configuration parameter to shut down automatically
 */
@Singleton
public class RegistryTester
{
    /**
     * Convenience function for getting an integral property. Also logs the value.
     */
    public static int intProp (String name, int defaultVal)
    {
        String val = System.getProperty(name);
        if (val == null) {
            log.info("Property " + name + " is " + defaultVal);
            return defaultVal;
        }
        int ival = Integer.parseInt(val);
        log.info("Property " + name + " is " + ival);
        return ival;
    }

    /**
     * Creates a new server and runs the registry tester on it.
     */
    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new TestServer.PresentsModule());
        TestServer server = injector.getInstance(TestServer.class);
        RegistryTester tester = injector.getInstance(RegistryTester.class);

        try {
            server.init(injector);
            tester.start();
            server.run();

        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
        }
    }

    /**
     * Creates a new registry tester.
     */
    @Inject public RegistryTester (TestServer server, Lifecycle cycle)
    {
        _server = server;

        _maxAgents = intProp("maxAgents", 500);
        _numBureaus = intProp("numBureaus", 5);
        _maxOps = intProp("maxOps", 5);
        _killBureauChance = intProp("killBureauChance", 1);
        _createChance = intProp("createChance", 70);
        _minDelay = intProp("minDelay", 500);
        _maxDelay = intProp("maxDelay", 2000);
        _clientTarget = System.getProperty("clientTarget");
        if (_clientTarget == null) {
            _clientTarget = "bureau-runclient";
        }

        // stop the tests when the server shuts down
        // TODO: this is not called on Ctrl-C, need a way to shut down gracefully
        cycle.addComponent(new Lifecycle.ShutdownComponent() {
            public void shutdown () {
                log.info("Shutting down tests");
                _stop = true;
            }
        });
    }

    /**
     * Starts the test thread.
     */
    public void start ()
    {
        _server.setClientTarget(_clientTarget);

        Thread thread = new Thread("Registry test thread") {
            @Override
            public void run () {
                log.info(getName() + " started");
                runTestThread();
                log.info(getName() + " stopped");
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

        log.info("Running tests, seed is " + seed);

        // runnable that generates N requests to create or destroy agents
        Runnable doOp = new Runnable() {
            public void run () {
                int ops = 1 + _rng1.nextInt(_maxOps);
                log.info("Starting " + ops + " agent requests");
                for (int i = 0; i < ops; ++i) {
                    randomlyDoOperation();
                }
                log.info("Finished " + ops + " agent requests");
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
            _omgr.postRunnable(doOp);
        }

        // clean up
        _omgr.postRunnable(new Runnable() {
            public void run () {
                for (AgentObject obj : _agents) {
                    _bureauReg.destroyAgent(obj);
                }
            }
        });
    }

    /**
     * Chooses between killing a bureau connection or creating or destroying an agent.
     */
    protected void randomlyDoOperation ()
    {
        if (_rng1.nextInt(100) < _killBureauChance) {
            log.info("Killing a bureau");
            PresentsSession bureau = getRandomBureau();
            if (bureau == null) {
                log.info("No bureaus to kill right now");
                return;
            }

            String id = ((BureauCredentials)bureau.getCredentials()).clientId;
            log.info("Killing bureau " + id);
            bureau.endSession();

            for (Iterator<AgentObject> i = _agents.iterator(); i.hasNext();) {
                AgentObject agent = i.next();
                if (agent.bureauId.equals(id)) {
                    log.info("Removing agent " + agent.getOid());
                    i.remove();
                }
            }
        } else {
            randomlyCreateOrDestroyAgent();
        }
    }

    protected PresentsSession getRandomBureau ()
    {
        boolean[] tried = new boolean[_numBureaus];
        for (int dead = 0; dead < _numBureaus;) {
            int index = _rng1.nextInt(_numBureaus);
            if (tried[index]) {
                continue;
            }
            String id = "test-" + (index + 1);
            PresentsSession client = _bureauReg.lookupClient(id);
            if (client == null) {
                ++dead;
            } else {
                return client;
            }
            tried[index] = true;
        }
        return null;
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
            log.info("Destroying agent " + toRemove.getOid());
            _bureauReg.destroyAgent(toRemove);
        }
        else {
            AgentObject added = create(_rng1.nextInt(_numBureaus) + 1);
            _agents.add(added);
            log.info("Created agent " + added.getOid());
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
        _bureauReg.startAgent(obj);
        return obj;
    }

    @Inject protected RootDObjectManager _omgr;
    @Inject protected BureauRegistry _bureauReg;

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

    // chance of killing a buerau (if one exists)
    protected int _killBureauChance;

    // chance of creating a new agent if limit has not been reached
    protected int _createChance;

    // minimum delay between batches of requests
    protected int _minDelay;

    // maximum delay between batches of requests
    protected int _maxDelay;

    // ant target to use to kick off new bureaus
    protected String _clientTarget;
}
