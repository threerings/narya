//
// $Id: SimulatorService.java,v 1.1 2002/08/14 19:07:50 mdb Exp $

package com.threerings.micasa.simulator.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.parlor.game.GameConfig;

/**
 * Provides access to simulator invocation services.
 */
public interface SimulatorService extends InvocationService
{
    /**
     * Requests that a new game be created.
     *
     * @param client a connected, operational client instance.
     * @param config the game config for the game to be created.
     * @param simClass the class name of the simulant to create.
     * @param playerCount the number of players in the game.
     */
    public void createGame (Client client, GameConfig config,
                            String simClass, int playerCount);
}
