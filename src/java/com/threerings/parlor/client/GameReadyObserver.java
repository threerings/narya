//
// $Id: GameReadyObserver.java,v 1.1 2002/02/09 11:23:49 mdb Exp $

package com.threerings.parlor.client;

/**
 * Used to inform interested parties when the {@link ParlorDirector}
 * receives a game ready notification. The observers can ratify the
 * decision to head directly into the game or can take responsibility
 * themselves for doing so.
 */
public interface GameReadyObserver
{
    /**
     * Called when a game ready notification is received.
     *
     * @param gameOid the place oid of the ready game.
     *
     * @return if the observer returns true from this method, the parlor
     * director assumes they will take care of entering the game room
     * after performing processing of their own. If all observers return
     * false, the director will enter the game room automatically.
     */
    public boolean receivedGameReady (int gameOid);
}
