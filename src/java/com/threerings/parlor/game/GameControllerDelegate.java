//
// $Id: GameControllerDelegate.java,v 1.1 2002/02/13 03:21:28 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.crowd.client.PlaceControllerDelegate;

/**
 * Extends the {@link PlaceControllerDelegate} mechanism with game
 * controller specific methods.
 */
public class GameControllerDelegate extends PlaceControllerDelegate
{
    /**
     * Provides the delegate with a reference to the game controller for
     * which it is delegating.
     */
    public GameControllerDelegate (GameController ctrl)
    {
        super(ctrl);
    }

    /**
     * Called when the game transitions to the <code>IN_PLAY</code>
     * state. This happens when all of the players have arrived and the
     * server starts the game.
     */
    public void gameDidStart ()
    {
    }

    /**
     * Called when the game transitions to the <code>GAME_OVER</code>
     * state. This happens when the game reaches some end condition by
     * normal means (is not cancelled or aborted).
     */
    public void gameDidEnd ()
    {
    }

    /**
     * Called when the game was cancelled for some reason.
     */
    public void gameWasCancelled ()
    {
    }
}
