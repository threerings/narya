//
// $Id: SimulatorServer.java,v 1.5 2002/02/05 22:12:42 mdb Exp $

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

    /**
     * Called by the simulator manager to map a username to a particular
     * body object. This should only be called from the dobjmgr thread.
     */
    public void fakeBodyMapping (String username, BodyObject bodobj);
}
