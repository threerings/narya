//
// $Id: SimulatorServer.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.server;

import com.threerings.crowd.server.CrowdServer;
import com.threerings.parlor.server.ParlorManager;

import com.threerings.micasa.Log;

/**
 * This class is the main entry point and general organizer of everything
 * that goes on in the Simulator game server process.
 */
public class SimulatorServer extends CrowdServer
{
    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

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

        // initialize our managers
        parmgr.init(config, invmgr);
        simmgr.init(config, invmgr);

        Log.info("Simulator server initialized.");
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
