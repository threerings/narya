//
// $Id: SimpleServer.java,v 1.3 2002/03/05 05:33:25 mdb Exp $

package com.threerings.micasa.simulator.server;

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
    public void init ()
        throws Exception
    {
        super.init();

        // create the simulator manager
        SimulatorManager simmgr = new SimulatorManager();
        simmgr.init(config, invmgr, plreg, clmgr, omgr, this);
    }
}
