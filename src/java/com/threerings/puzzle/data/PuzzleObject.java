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

package com.threerings.puzzle.data;

import com.threerings.parlor.game.GameObject;

/**
 * Extends the basic {@link GameObject} to add individual player
 * status. Puzzle games typically contain numerous players that may be
 * knocked out of the game while the overall game continues on, thereby
 * necessitating this second level of game status.
 */
public class PuzzleObject extends GameObject
    implements PuzzleCodes
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>puzzleGameService</code> field. */
    public static final String PUZZLE_GAME_SERVICE = "puzzleGameService";

    /** The field name of the <code>difficulty</code> field. */
    public static final String DIFFICULTY = "difficulty";

    /** The field name of the <code>playerStatus</code> field. */
    public static final String PLAYER_STATUS = "playerStatus";

    /** The field name of the <code>summaries</code> field. */
    public static final String SUMMARIES = "summaries";

    /** The field name of the <code>seed</code> field. */
    public static final String SEED = "seed";
    // AUTO-GENERATED: FIELDS END

    /** The player status constant for a player whose game is in play. */
    public static final int PLAYER_IN_PLAY = 0;

    /** The player status constant for a player whose has been knocked out
     * of the game. */
    public static final int PLAYER_KNOCKED_OUT = 1;

    /** Provides general puzzle game invocation services. */
    public PuzzleGameMarshaller puzzleGameService;

    /** The puzzle difficulty level. */
    public int difficulty;

    /**  The status of each of the players in the game. The status value
     * is one of {@link #PLAYER_KNOCKED_OUT} or {@link
     * #PLAYER_IN_PLAY}. */
    public int[] playerStatus;

    /** Summaries of the boards of all players in this puzzle (may be null
     * if the puzzle doesn't support individual player boards). */
    public BoardSummary[] summaries;

    /** The seed used to germinate the boards. */
    public long seed;

    /**
     * Returns the number of active players in the game.
     */
    public int getActivePlayerCount ()
    {
        int count = 0;
        int size = players.length;
        for (int ii = 0; ii < size; ii++) {
            if (isActivePlayer(ii)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns whether the given player is still an active player, e.g.,
     * their game has not ended.
     */
    public boolean isActivePlayer (int pidx)
    {
        return (isOccupiedPlayer(pidx) &&
                playerStatus != null && playerStatus[pidx] == PLAYER_IN_PLAY);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>puzzleGameService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPuzzleGameService (PuzzleGameMarshaller value)
    {
        PuzzleGameMarshaller ovalue = this.puzzleGameService;
        requestAttributeChange(
            PUZZLE_GAME_SERVICE, value, ovalue);
        this.puzzleGameService = value;
    }

    /**
     * Requests that the <code>difficulty</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setDifficulty (int value)
    {
        int ovalue = this.difficulty;
        requestAttributeChange(
            DIFFICULTY, new Integer(value), new Integer(ovalue));
        this.difficulty = value;
    }

    /**
     * Requests that the <code>playerStatus</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPlayerStatus (int[] value)
    {
        int[] ovalue = this.playerStatus;
        requestAttributeChange(
            PLAYER_STATUS, value, ovalue);
        this.playerStatus = (value == null) ? null : (int[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>playerStatus</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setPlayerStatusAt (int value, int index)
    {
        int ovalue = this.playerStatus[index];
        requestElementUpdate(
            PLAYER_STATUS, index, new Integer(value), new Integer(ovalue));
        this.playerStatus[index] = value;
    }

    /**
     * Requests that the <code>summaries</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setSummaries (BoardSummary[] value)
    {
        BoardSummary[] ovalue = this.summaries;
        requestAttributeChange(
            SUMMARIES, value, ovalue);
        this.summaries = (value == null) ? null : (BoardSummary[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>summaries</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setSummariesAt (BoardSummary value, int index)
    {
        BoardSummary ovalue = this.summaries[index];
        requestElementUpdate(
            SUMMARIES, index, value, ovalue);
        this.summaries[index] = value;
    }

    /**
     * Requests that the <code>seed</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setSeed (long value)
    {
        long ovalue = this.seed;
        requestAttributeChange(
            SEED, new Long(value), new Long(ovalue));
        this.seed = value;
    }
    // AUTO-GENERATED: METHODS END
}
