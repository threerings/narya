//
// $Id: SimulatorServer.java,v 1.6 2002/03/05 05:33:25 mdb Exp $

package com.threerings.micasa.simulator.server;

import com.threerings.crowd.data.BodyObject;

/**
 * The simulator manager needs a mechanism for faking body object
 * registrations, which is provided by implementations of this interface.
 */
public interface SimulatorServer
{
    /**
     * Called to initialize this server instance.
     *
     * @exception Exception thrown if anything goes wrong initializing the
     * server.
     */
    public void init () throws Exception;

    /**
     * Called to perform the main body of server processing. This is
     * called from the server thread and should do the simulator server's
     * primary business.
     */
    public void run ();
}
