//
// $Id: GameManagerDelegate.java,v 1.1 2002/02/13 03:21:28 mdb Exp $

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
     * Called by the game manager after the game ended.
     */
    public void gameDidEnd ()
    {
    }
}
