//
// $Id: SimulatorDirector.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.parlor.game.GameConfig;

public class SimulatorDirector implements SimulatorCodes
{
    /**
     * Requests that a new game be created.
     *
     * @param client a connected, operational client instance.
     * @param config the game config for the game to be created.
     * @param simClass the class name of the simulant to create.
     * @param playerCount the number of players in the game.
     *
     * @return the invocation request id of the generated request.
     */
    public static int createGame (
        Client client, GameConfig config, String simClass, int playerCount)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] {
            config, simClass, new Integer(playerCount) };
        return invdir.invoke(MODULE_NAME, CREATE_GAME_REQUEST, args, null);
    }
}
