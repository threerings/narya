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

import com.threerings.io.SimpleStreamableObject;

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
public abstract class BoardSummary extends SimpleStreamableObject
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
     * Sets the board associated with this board summary, causing
     * an immediate update to this summary.
     */
    public void setBoard (Board board)
    {
        _board = board;
        summarize(); // immediately summarize the new board
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
