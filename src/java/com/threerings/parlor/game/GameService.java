//
// $Id: GameService.java,v 1.1 2002/08/14 19:07:53 mdb Exp $

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
}
