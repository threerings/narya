//
// $Id: GameControllerDelegate.java,v 1.2 2002/04/14 00:26:05 mdb Exp $

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

    /**
     * Called to give derived classes a chance to display animations, send
     * a final packet, or do any other business they care to do when the
     * game is about to reset.
     */
    public void gameWillReset ()
    {
    }
}
