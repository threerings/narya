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

package com.threerings.puzzle.drop.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.samskivert.util.StringUtil;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropPieceCodes;

/**
 * Handles dropping pieces in a board.
 */
public class PieceDropper
    implements DropPieceCodes
{
    /**
     * A class to hold information detailing the pieces to be dropped
     * in a particular column.
     */
    public static class PieceDropInfo
    {
	/** The starting row of the bottom piece being dropped. */
	public int row;

	/** The column number. */
	public int col;

	/** The distance to drop the pieces. */
	public int dist;

	/** The pieces to be dropped. */
	public int[] pieces;

        /**
         * Constructs a piece drop info object.
         */
	public PieceDropInfo (int col, int row, int dist)
	{
	    this.col = col;
	    this.row = row;
	    this.dist = dist;
	}

        /** Returns a string representation of this instance. */
	public String toString ()
	{
            return StringUtil.fieldsToString(this);
	}
    }

    /**
     * Called to inform a drop observer that a piece has been dropped.
     */
    public static interface DropObserver
    {
        /** Indicates that the specified piece was dropped. */
        public void pieceDropped (int piece, int sx, int sy, int dx, int dy);
    }

    /**
     * Constructs a piece dropper that uses the supplied piece drop logic
     * to specialise itself for a particular puzzle.
     */
    public PieceDropper (PieceDropLogic logic)
    {
        _logic = logic;
    }

    /**
     * Effects any drops possible on the supplied board (modifying the
     * board in the progress) and notifying the supplied drop observer of
     * those drops.
     *
     * @return the number of pieces dropped.
     */
    public int dropPieces (DropBoard board, DropObserver drobs)
    {
        int dropped = 0, bhei = board.getHeight(), bwid = board.getWidth();
	for (int yy = bhei - 1; yy >= 0; yy--) {
	    for (int xx = 0; xx < bwid; xx++) {
                dropped += dropPieces(board, xx, yy, drobs);
	    }
	}

        // if the board wants pieces to be dropped in to fill the gaps, do
        // that now
        if (_logic.boardAlwaysFilled()) {
            for (int xx = 0; xx < bwid; xx++) {
                int dist = board.getDropDistance(xx, -1);
                for (int ii = 0; ii < dist; ii++) {
                    int yy = (-1 - ii);
                    int piece = board.getNextPiece();
                    if (piece != PIECE_NONE) {
                        drop(board, piece, xx, yy, yy + dist, drobs);
                        dropped++;
                    }
                }
            }
        }

        return dropped;
    }

    /**
     * Computes and effects the drop for the specified piece and any
     * associated attached pieces. The supplied observer is notified of
     * all drops.
     */
    protected int dropPieces (
        DropBoard board, int xx, int yy, DropObserver drobs)
    {
	// skip empty or fixed pieces
	int piece = board.getPiece(xx, yy);
        if (!_logic.isDroppablePiece(piece)) {
	    return 0;
	}

        int dropped = 0;
	if (_logic.isConstrainedPiece(piece)) {
            // find out where this constrained block starts and ends
            int start = _logic.getConstrainedEdge(board, xx, yy, LEFT);
            int end = _logic.getConstrainedEdge(board, xx, yy, RIGHT);
            int bwid = board.getWidth();
            if (start < 0 || end >= bwid) {
                Log.warning("Board reported bogus constrained edge " +
                            "[x=" + xx + ", y=" + yy +
                            ", start=" + start + ", end=" + end + "].");
                board.dump();
                start = Math.max(start, 0);
                end = Math.min(end, bwid);
            }

            // get the smallest drop distance across all of the block columns
            int dist = board.getHeight() - 1;
            for (int xpos = start; xpos <= end; xpos++) {
                dist = Math.min(dist, board.getDropDistance(xpos, yy));
            }
	    if (dist == 0) {
		return 0;
	    }

	    // scoot along the bottom edge of the block, noting the drop
	    // for each column
            for (int xpos = start; xpos <= end; xpos++) {
                piece = board.getPiece(xpos, yy);
                drop(board, piece, xpos, yy, yy + dist, drobs);
                dropped++;
	    }

	} else {
	    // get the distance to drop the pieces
	    int dist = board.getDropDistance(xx, yy);
	    if (dist == 0) {
		return 0;
	    }
            drop(board, piece, xx, yy, yy + dist, drobs);
            dropped++;
	}

        return dropped;
    }

    /** Helpy helper function. */
    protected final void drop (DropBoard board, int piece,
                               int xx, int yy, int ty, DropObserver drobs)
    {
        // don't try to clear things out if we're filling in from off-board
        if (yy >= 0) {
            board.setPiece(xx, yy, PIECE_NONE);
        }
        board.setPiece(xx, ty, piece);
        if (drobs != null) {
            drobs.pieceDropped(piece, xx, yy, xx, ty);
        }
    }

    /** Allows puzzle-specific customizations. */
    protected PieceDropLogic _logic;
}
