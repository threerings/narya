//
// $Id: BoardSummary.java,v 1.2 2004/08/23 21:10:01 mdb Exp $

package com.threerings.puzzle.data;

import com.threerings.io.TrackedStreamableObject;

/**
 * Provides summarized data representing a player's board in a puzzle
 * game.  Board summaries are maintained by the puzzle server and are
 * periodically sent to the clients to give them a view into how well
 * their opponent(s) are doing.  The data required to marshal a board
 * summary object should be notably smaller in size than what would be
 * required to marshal the entire associated {@link Board}.
 *
 * <p> Note all non-transient members of this and derived classes will
 * automatically be serialized when the summary is sent over the wire.
 */
public abstract class BoardSummary extends TrackedStreamableObject
{
    /**
     * Constructs an empty board summary for use when un-serializing.
     */
    public BoardSummary ()
    {
        // nothing for now
    }

    /**
     * Constructs a board summary that retrieves full board information
     * from the supplied board when summarizing.
     */
    public BoardSummary (Board board)
    {
        setBoard(board);
    }

    /**
     * Sets the board associated with this board summary.
     */
    public void setBoard (Board board)
    {
        _board = board;
    }

    /**
     * Called by the {@link
     * com.threerings.puzzle.server.PuzzleManager} to refresh the
     * board summary information by studying the associated board
     * contents.
     */
    public abstract void summarize ();

    /** The board that we're summarizing.  This is only valid on the
     * server, and on the client only for the actual player's board. */
    protected transient Board _board;
}
