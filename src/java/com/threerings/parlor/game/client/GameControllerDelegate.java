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

package com.threerings.parlor.game.client;

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
