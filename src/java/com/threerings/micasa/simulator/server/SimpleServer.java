//
// $Id: SimpleServer.java,v 1.1 2002/02/05 22:12:42 mdb Exp $

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
    public void fakeBodyMapping (String username, BodyObject bodobj)
    {
        _bodymap.put(username, bodobj);
    }
}
