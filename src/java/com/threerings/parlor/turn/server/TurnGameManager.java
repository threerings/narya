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

package com.threerings.parlor.turn.server;

import com.threerings.util.Name;

import com.threerings.parlor.game.server.GameManager;

/**
 * A game manager that wishes to make use of the turn game services should
 * implement this interface and create a {@link TurnGameManagerDelegate}
 * which will perform the basic turn game processing and call back to the
 * main manager via this interface.
 *
 * <p> The basic flow of a turn-based game is as follows:
 * <pre>
 * GameManager.gameWillStart()
 * GameManager.gameDidStart()
 *   TurnGameManagerDelegate.setFirstTurnHolder()
 *   TurnGameManagerDelegate.startTurn()
 *     TurnGameManager.turnWillStart()
 *   TurnGameManagerDelegate.endTurn()
 *     TurnGameManager.turnDidEnd()
 *   TurnGameManagerDelegate.setNextTurnHolder()
 *   TurnGameManagerDelegate.startTurn()
 *     ...
 * GameManager.endGame()
 * </pre>
 */
public interface TurnGameManager
{
    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public Name getPlayerName (int index);

    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public int getPlayerIndex (Name username);

    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public int getPlayerCount ();

    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public boolean isActivePlayer (int pidx);

    /**
     * Called when we are about to start the next turn. Implementations
     * can do whatever pre-start turn activities need to be done.
     */
    public void turnWillStart ();

    /**
     * Called when we have started the next turn. Implementations can do
     * whatever post-start turn activities need to be done.
     */
    public void turnDidStart ();

    /**
     * Called when the turn was ended. Implementations can perform any
     * post-turn processing (like updating scores, etc.).
     */
    public void turnDidEnd ();
}
