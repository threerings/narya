//
// $Id: SimulatorProvider.java,v 1.3 2003/01/21 22:10:18 mdb Exp $

package com.threerings.micasa.simulator.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.parlor.game.GameConfig;

import com.threerings.micasa.Log;

/**
 * The simulator provider handles game creation requests on the server
 * side, passing them off to the {@link SimulatorManager}.
 */
public class SimulatorProvider
    implements InvocationProvider
{
    /**
     * Constructs a simulator provider.
     */
    public SimulatorProvider (SimulatorManager simmgr)
    {
        _simmgr = simmgr;
    }

    /**
     * Processes a request from the client to create a new game.
     */
    public void createGame (ClientObject caller, GameConfig config,
                            String simClass, int playerCount)
    {
        Log.info("handleCreateGameRequest [caller=" + caller.who() +
                 ", config=" + config + ", simClass=" + simClass +
                 ", playerCount=" + playerCount + "].");

        _simmgr.createGame((BodyObject)caller, config, simClass, playerCount);
    }

    /** The simulator manager. */
    protected SimulatorManager _simmgr;
}
