//
// $Id: SimpleServer.java,v 1.2 2002/02/05 22:57:10 mdb Exp $

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

    // documentation inherited
    public void fakeBodyMapping (String username, BodyObject bodobj)
    {
        _bodymap.put(username, bodobj);
    }
}
