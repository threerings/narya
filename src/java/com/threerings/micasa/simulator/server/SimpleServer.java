//
// $Id: SimpleServer.java,v 1.5 2002/06/10 19:16:41 shaper Exp $

package com.threerings.micasa.simulator.server;

import com.samskivert.util.ResultListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

/**
 * A simple simulator server implementation that extends the crowd server
 * and provides no special functionality.
 */
public class SimpleServer extends CrowdServer
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
