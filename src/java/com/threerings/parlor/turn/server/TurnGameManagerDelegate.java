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
import com.threerings.util.RandomUtil;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.game.server.GameManagerDelegate;

import com.threerings.parlor.turn.data.TurnGameObject;

/**
 * Performs the server-side turn-based game processing for a turn based
 * game. Game managers which wish to make use of the turn services must
 * implement {@link TurnGameManager} and either create an instance of this
 * class, or an instance of a derivation which customizes the behavior,
 * either of which would be passed to {@link GameManager#addDelegate} to
 * be activated.
 */
public class TurnGameManagerDelegate extends GameManagerDelegate
{
    /**
     * Constructs a delegate that will manage the turn game state and call
     * back to the supplied {@link TurnGameManager} implementation to let
     * it in on the progression of the game.
     */
    public TurnGameManagerDelegate (TurnGameManager tgmgr)
    {
        super((GameManager)tgmgr);
        _tgmgr = tgmgr;
    }

    /**
     * Returns the index of the current turn holder as configured in the
     * game object.
     *
     * @return the index into the players array of the current turn holder
     * or <code>-1</code> if there is no current turn holder.
     */
    public int getTurnHolderIndex ()
    {
        return _tgmgr.getPlayerIndex(_turnGame.getTurnHolder());
    }

    /**
     * Called to start the next turn. It calls {@link
     * TurnGameManager#turnWillStart} to allow our owning manager to
     * perform any pre-start turn processing, sets the turn holder that
     * was configured either when the game started or when finishing up
     * the last turn, and then calls {@link TurnGameManager#turnDidStart}
     * to allow the manager to perform any post-start turn
     * processing. This assumes that a valid turn holder has been
     * assigned. If some pre-game preparation needs to take place in a
     * non-turn-based manner, this function should not be called until it
     * is time to start the first turn.
     */
    public void startTurn ()
    {
        // sanity check
        if (_turnIdx < 0 || _turnIdx >= _turnGame.getPlayers().length) {
            Log.warning("startTurn() called with invalid turn index " +
                        "[turnIdx=" + _turnIdx + "].");
            // abort, abort
            return;
        }

        // get the player name and sanity-check again
        Name name = _tgmgr.getPlayerName(_turnIdx);
        if (name == null) {
            Log.warning("startTurn() called with invalid player " +
                        "[turnIdx=" + _turnIdx + "].");
            return;
        }

        // do pre-start processing
        _tgmgr.turnWillStart();

        // and set the turn indicator accordingly
        _turnGame.setTurnHolder(name);

        // do post-start processing
        _tgmgr.turnDidStart();
    }

    /**
     * Called to end the turn. Whatever indication a game manager has that
     * the turn has ended (probably the submission of a valid move of some
     * sort by the turn holding player), it should call this function to
     * cause this turn to end and the next to begin.
     *
     * <p> If the next turn should not be started immediately after this
     * turn, the game manager should arrange for {@link
     * #setNextTurnHolder} to set the {@link #_turnIdx} field to
     * <code>-1</code> which will cause us not to start the next turn. It
     * can then call {@link GameManager#endGame} if the game is over or do
     * whatever else it needs to do outside the context of the turn flow.
     * To start things back up again it would set {@link #_turnIdx} to the
     * next turn holder and call {@link #startTurn} itself.
     */
    public void endTurn ()
    {
        // let the manager know that the turn is over
        _tgmgr.turnDidEnd();

        // figure out who's up next
        setNextTurnHolder();

        // and start the next turn if desired
        if (_turnIdx != -1) {
            startTurn();
        }
    }

    // documentation inherited
    public void didStartup (PlaceObject plobj)
    {
        _turnGame = (TurnGameObject)plobj;
    }

    /**
     * This should be called from {@link GameManager#gameDidStart} to let
     * the turn delegate perform start of game processing.
     */
    public void gameDidStart ()
    {
        // figure out who will be first
        setFirstTurnHolder();

        // and start the first turn if we should apparently do so
        if (_turnIdx != -1) {
            startTurn();
        }
    }

    /**
     * This is called to determine which player will take the first
     * turn. The default implementation chooses a player at random.
     */
    protected void setFirstTurnHolder ()
    {
        int size = _turnGame.getPlayers().length;
        do {
            _turnIdx = RandomUtil.getInt(size);
        } while (_tgmgr.getPlayerName(_turnIdx) == null);
    }

    /**
     * This is called to determine which player will next hold the turn.
     * The default implementation simply rotates through the players in
     * order, but some games may need to mess with the turn from time to
     * time. This should update the <code>_turnIdx</code> field, not set
     * the turn holder field in the game object directly.
     */
    protected void setNextTurnHolder ()
    {
        // stick with the current player if they're the only participant
        if (_tgmgr.getPlayerCount() == 1) {
            return;
        }

        // find the next occupied player slot
        int size = _turnGame.getPlayers().length;
        int oturnIdx = _turnIdx;
        do {
            _turnIdx = (_turnIdx + 1) % size;
        } while (_tgmgr.getPlayerName(_turnIdx) == null);
    }

    /** The game manager for which we are delegating. */
    protected TurnGameManager _tgmgr;

    /** A reference to our game object. */
    protected TurnGameObject _turnGame;

    /** The player index of the current turn holder or <code>-1</code> if
     * it's no one's turn. */
    protected int _turnIdx = -1;
}
