//
// $Id: SimpleServer.java,v 1.6 2002/08/15 23:21:45 mdb Exp $

package com.threerings.micasa.simulator.server;

import com.samskivert.util.ResultListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.micasa.server.MiCasaServer;

/**
 * A simple simulator server implementation that extends the MiCasa server
 * and provides no special functionality.
 */
public class SimpleServer extends MiCasaServer
    implements SimulatorServer
{
    // documentation inherited
    public void init (ResultListener obs)
        throws Exception
    {
        super.init();

        // create the simulator manager
        SimulatorManager simmgr = new SimulatorManager();
        simmgr.init(invmgr, plreg, clmgr, omgr, this);

        if (obs != null) {
            // let the initialization observer know that we've started up
            obs.requestCompleted(this);
        }
    }
}
