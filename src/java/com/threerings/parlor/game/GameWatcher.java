//
// $Id: GameWatcher.java,v 1.1 2002/12/16 05:06:58 shaper Exp $

package com.threerings.parlor.game;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

/**
 * An abstract convenience class used server-side to keep an eye on a game
 * and perform a one-time game-over activity when the game ends.  Classes
 * that care to make use of the game watcher should create an instance,
 * implement {@link #gameDidEnd}, and pass the instance to the place
 * registry in the call to {@link PlaceRegistry#createPlace} when the
 * puzzle is created.
 */
public abstract class GameWatcher
    implements PlaceRegistry.CreationObserver, AttributeChangeListener
{
    // documentation inherited
    public void placeCreated (PlaceObject place, PlaceManager pmgr)
    {
        _gameobj = (GameObject)place;
        _gameobj.addListener(this);
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(GameObject.STATE)) {
            // if we transitioned to a non-in-play state, the game has
            // completed
            if (!_gameobj.isInPlay()) {
                gameDidEnd(_gameobj);
                // clean up
                _gameobj.removeListener(this);
                _gameobj = null;
            }
        }
    }

    /**
     * Called when the game ends to give derived classes a chance to
     * engage in their game-over antics.
     */
    protected abstract void gameDidEnd (GameObject gameobj);

    /** The game object we're observing. */
    protected GameObject _gameobj;
}
