//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.parlor.game.server;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.game.data.GameObject;

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
                try {
                    gameDidEnd(_gameobj);
                } finally {
                    _gameobj.removeListener(this);
                    _gameobj = null;
                }
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
