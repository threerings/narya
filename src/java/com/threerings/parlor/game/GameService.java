//
// $Id: GameService.java,v 1.2 2002/09/06 22:52:27 shaper Exp $

package com.threerings.parlor.game;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides services used by game clients to request that actions be taken
 * by the game manager.
 */
public interface GameService extends InvocationService
{
    /**
     * Lets the game manager know that the calling player is in the game
     * room and ready to play.
     */
    public void playerReady (Client client);

    /**
     * Asks the game manager to start the party game.  This should only be
     * called for party games, and then only by the creating player after
     * any other game-specific starting prerequisites (e.g., a required
     * number of players) have been fulfilled.
     */
    public void startPartyGame (Client client);
}
