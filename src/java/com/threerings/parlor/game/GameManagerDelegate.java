//
// $Id: GameManagerDelegate.java,v 1.4 2002/04/19 21:40:38 ray Exp $

package com.threerings.parlor.game;

import com.threerings.crowd.server.PlaceManagerDelegate;

/**
 * Extends the {@link PlaceManagerDelegate} mechanism with game manager
 * specific methods.
 */
public class GameManagerDelegate extends PlaceManagerDelegate
{
    /**
     * Provides the delegate with a reference to the game manager for
     * which it is delegating.
     */
    public GameManagerDelegate (GameManager gmgr)
    {
        super(gmgr);
    }

    /**
     * Called by the game manager when the game is about to start.
     */
    public void gameWillStart ()
    {
    }

    /**
     * Called by the game manager after the game was started.
     */
    public void gameDidStart ()
    {
    }

    /**
     * Called by the manager when we should do some AI. Only called while
     * the game is IN_PLAY.
     *
     * @param pidx the player index to fake some gameplay for.
     * @param skill the base skill level of the AI (0 - 100 inclusive).
     */
    public void tickAI (int pidx, byte skill)
    {
    }

    /**
     * Called by the game manager after the game ended.
     */
    public void gameDidEnd ()
    {
    }

    /**
     * Called when the game is about to reset, but before any other
     * clearing out of game data has taken place.  Derived classes should
     * override this if they need to perform some pre-reset activities.
     */
    public void gameWillReset ()
    {
    }

    /**
     * Called after the game has been reset.  Derived classes can override
     * this to put whatever wheels they might need into motion now that
     * the game is reset.
     */
    public void gameDidReset ()
    {
    }
}
