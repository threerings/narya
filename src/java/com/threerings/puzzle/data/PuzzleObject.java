//
// $Id: PuzzleObject.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

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

    /** The player status constant for a player whose game is in play. */
    public static final int PLAYER_IN_PLAY = 0;

    /** The player status constant for a player whose has been knocked out
     * of the game. */
    public static final int PLAYER_KNOCKED_OUT = 1;

    /** Provides general puzzle game invocation services. */
    public PuzzleGameMarshaller puzzleGameService;

    /** The puzzle difficulty level. */
    public int difficulty = DEFAULT_DIFFICULTY;

    /**  The status of each of the players in the game. The status value
     * is one of {@link #PLAYER_KNOCKED_OUT} or {@link
     * #PLAYER_IN_PLAY}. */
    public int[] playerStatus;

    /** Summaries of the boards of all players in this puzzle (may be null
     * if the puzzle doesn't support individual player boards). */
    public BoardSummary[] summaries;

    /** The seed used to germinate the boards. */
    public long seed;

    // documentation inherited
    public boolean shouldBroadcast ()
    {
        // we do not broadcast to puzzles because the users will get it
        // on their scene objects
        return false;
    }

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

    /**
     * Requests that the <code>puzzleGameService</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPuzzleGameService (PuzzleGameMarshaller puzzleGameService)
    {
        requestAttributeChange(PUZZLE_GAME_SERVICE, puzzleGameService);
        this.puzzleGameService = puzzleGameService;
    }

    /**
     * Requests that the <code>difficulty</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setDifficulty (int difficulty)
    {
        requestAttributeChange(DIFFICULTY, new Integer(difficulty));
        this.difficulty = difficulty;
    }

    /**
     * Requests that the <code>playerStatus</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPlayerStatus (int[] playerStatus)
    {
        requestAttributeChange(PLAYER_STATUS, playerStatus);
        this.playerStatus = playerStatus;
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>playerStatus</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPlayerStatusAt (int value, int index)
    {
        requestElementUpdate(PLAYER_STATUS, new Integer(value), index);
        this.playerStatus[index] = value;
    }

    /**
     * Requests that the <code>summaries</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setSummaries (BoardSummary[] summaries)
    {
        requestAttributeChange(SUMMARIES, summaries);
        this.summaries = summaries;
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>summaries</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setSummariesAt (BoardSummary value, int index)
    {
        requestElementUpdate(SUMMARIES, value, index);
        this.summaries[index] = value;
    }

    /**
     * Requests that the <code>seed</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setSeed (long seed)
    {
        requestAttributeChange(SEED, new Long(seed));
        this.seed = seed;
    }
}
