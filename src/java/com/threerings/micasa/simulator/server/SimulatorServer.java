//
// $Id: SimulatorServer.java,v 1.3 2001/12/20 01:11:18 shaper Exp $

package com.threerings.micasa.simulator.server;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.micasa.Log;

/**
 * This class is the main entry point and general organizer of everything
 * that goes on in the Simulator game server process.
 */
public class SimulatorServer extends CrowdServer
{
    /** The simulator manager in operation on this server. */
    public static SimulatorManager simmgr = new SimulatorManager();

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the base server initialization
        super.init();

        // initialize the manager
        simmgr.init(config, invmgr);

        Log.info("Simulator server initialized.");
    }

    /**
     * Called by the simulator manager to map a username to a particular
     * body object. This should only be called from the dobjmgr thread.
     *
     * <p> This is copied from {@link CrowdServer#mapBody} as that
     * implementation is protected and cannot be referenced by classes in
     * the simulator package, but we know what we're doing and so we
     * knowingly expose this functionality to other classes in our
     * package.
     */
    protected static void mapBody (String username, BodyObject bodobj)
    {
        _bodymap.put(username, bodobj);
    }

    public static void main (String[] args)
    {
        SimulatorServer server = new SimulatorServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }
}
