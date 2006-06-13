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
import java.util.List;

import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropBoard.PieceOperation;
import com.threerings.puzzle.drop.data.DropPieceCodes;
import com.threerings.puzzle.drop.data.SegmentInfo;

/**
 * Handles destroying contiguous piece segments in a drop board.
 */
public class PieceDestroyer
    implements DropPieceCodes
{
    /**
     * An interface to be implemented by specific puzzles to detail the
     * parameters and methodology by which pieces are destroyed in the
     * puzzle board.
     */
    public interface DestroyLogic
    {
        /**
         * Returns the minimum length of a contiguously piece segment that
         * should be destroyed.
         */
        public int getMinimumLength ();

        /**
         * Returns whether piece <code>a</code> is equivalent to piece
         * <code>b</code> for the purposes of including it in a contiguous
         * piece segment to be destroyed.
         */
        public boolean isEquivalent (int a, int b);
    }

    /**
     * Constructs a piece destroyer that destroys pieces as specified by
     * the supplied destroy logic.
     */
    public PieceDestroyer (DestroyLogic logic)
    {
        _logic = logic;
    }

    /**
     * Destroys all pieces in the given board that are in contiguous rows
     * or columns of pieces, returning a list of {@link SegmentInfo}
     * objects detailing the destroyed piece segments.  Note that a single
     * list is used internally to gather the segment info, and so callers
     * that care to modify the list should create their own copy; also,
     * the pieces in the segments may overlap, i.e., two segments may
     * contain the same piece.
     */
    public List destroyPieces (DropBoard board, PieceOperation destroyOp)
    {
        // find all horizontally-oriented destroyed segments
        int bwid = board.getWidth(), bhei = board.getHeight();
        _destroyed.clear();
        int end = bwid - _logic.getMinimumLength() + 1;
        for (int yy = (bhei - 1); yy >= 0; yy--) {
            int xx = 0;
            while (xx < end) {
                xx += findSegment(board, HORIZONTAL, xx, yy);
            }
        }

        // find all vertically-oriented destroyed segments
        end = _logic.getMinimumLength() - 2;
        for (int xx = 0; xx < bwid; xx++) {
            int yy = bhei - 1;
            while (yy > end) {
                yy -= findSegment(board, VERTICAL, xx, yy);
            }
        }

        // destroy the pieces
        int size = _destroyed.size();
        for (int ii = 0; ii < size; ii++) {
            SegmentInfo si = (SegmentInfo)_destroyed.get(ii);
            board.applyOp(si.dir, si.x, si.y, si.len, destroyOp);
        }

        return _destroyed;
    }

    /**
     * Searches for a contiguously colored piece segment with the
     * specified orientation and root coordinates in the supplied board
     * and returns the length of the segment traversed.
     */
    protected int findSegment (DropBoard board, int dir, int x, int y)
    {
        _lengthOp.reset();
        board.applyOp(dir, x, y, _lengthOp);
        int len = _lengthOp.getLength();
        if (len >= _logic.getMinimumLength()) {
            _destroyed.add(new SegmentInfo(dir, x, y, len));
        }
        return len;
    }

    /**
     * A piece operation that calculates the length of the contiguous
     * piece segment to which it is applied.
     */
    protected class SegmentLengthOperation
        implements PieceOperation
    {
        /**
         * Resets the operation for application to a new piece segment.
         */
        public void reset ()
        {
            _len = 0;
        }

        /**
         * Returns the length of the contiguous piece segment.
         */
        public int getLength ()
        {
            return _len;
        }

        // documentation inherited
        public boolean execute (DropBoard board, int col, int row)
        {
            int piece = board.getPiece(col, row);
            if (_len == 0) {
                _len = 1;
                _piece = piece;
                return (piece != PIECE_NONE);

            } else if (_logic.isEquivalent(piece, _piece)) {
                _len++;
                return true;

            } else {
                return false;
            }
        }

        /** The root segment piece. */
        protected int _piece;

        /** The segment length in pieces. */
        protected int _len;
    }

    /** The puzzle-specific destroy logic with which we do our business. */
    protected DestroyLogic _logic;

    /** The piece operation used to determine segment length. */
    protected SegmentLengthOperation _lengthOp = new SegmentLengthOperation();

    /** The list of destroyed piece segments. */
    protected ArrayList _destroyed = new ArrayList();
}
