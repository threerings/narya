//
// $Id: SimulatorProvider.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.server;

import com.threerings.presents.server.InvocationProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.parlor.game.GameConfig;

import com.threerings.micasa.Log;

/**
 * The simulator provider handles game creation requests on the server
 * side, passing them off to the {@link SimulatorManager}.
 */
public class SimulatorProvider
    extends InvocationProvider
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
    public void handleCreateGameRequest (
        BodyObject source, int invid, GameConfig config,
        String simClass, int playerCount)
    {
        Log.info("handleCreateGameRequest [source=" + source +
                 ", config=" + config + ", simClass=" + simClass +
                 ", playerCount=" + playerCount + "].");

        _simmgr.createGame(source, config, simClass, playerCount);
    }

    /** The simulator manager. */
    protected SimulatorManager _simmgr;
}
