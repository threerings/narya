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

package com.threerings.puzzle.drop.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.threerings.util.DirectionUtil;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.drop.client.DropControllerDelegate;
import com.threerings.puzzle.drop.util.DropBoardUtil;

/**
 * A class that provides for various useful logical operations to be
 * enacted on a two-dimensional board and provides an easier mechanism for
 * referencing pieces by position.
 */
public class DropBoard extends Board
    implements DropPieceCodes
{
    /** The rotation constant for rotation around a central piece. */
    public static final int RADIAL_ROTATION = 0;

    /** The rotation constant for rotation wherein the block occupies the
     * same columns when rotating. */
    public static final int INPLACE_ROTATION = 1;

    /** An operation that does naught but clear pieces, which proves to be
     * generally useful. */
    public static final PieceOperation CLEAR_OP = new PieceOperation () {
        public boolean execute (DropBoard board, int col, int row) {
            board.setPiece(col, row, PIECE_NONE);
            return true;
        }
    };

    /**
     * An interface to be implemented by classes that would like to apply
     * some operation to each piece in a column or row segment in the
     * board.
     */
    public interface PieceOperation
    {
        /**
         * Called for each piece in the board segment the operation is
         * being applied to.
         *
         * @return true if the operation should continue to be applied if
         * being applied to multiple pieces, or false if it should
         * terminate after this application.
         */
        public boolean execute (DropBoard board, int col, int row);
    }

    /**
     * Constructs an empty drop board for use when unserializing.
     */
    public DropBoard ()
    {
        this(null, 0, 0);
    }

    /**
     * Constructs a drop board of the given dimensions with its
     * pieces initialized to PIECE_NONE.
     */
    public DropBoard (int bwid, int bhei)
    {
        this(new int[bwid*bhei], bwid, bhei);
        fill(PIECE_NONE);
    }

    /**
     * Constructs a drop board of the given dimensions with its
     * pieces initialized to the given piece.
     */
    public DropBoard (int bwid, int bhei, int piece)
    {
        this(new int[bwid*bhei], bwid, bhei);
        fill(piece);
    }

    /**
     * Constructs a drop board with the given board and dimensions.
     */
    public DropBoard (int[] board, int bwid, int bhei)
    {
        _board = board;
        _bwid = bwid;
        _bhei = bhei;
    }

    /**
     * Returns the width of the board in columns.
     */
    public int getWidth()
    {
        return _bwid;
    }

    /**
     * Returns the height of the board in rows.
     */
    public int getHeight()
    {
        return _bhei;
    }

    /**
     * Returns the piece at the given column and row in the board.
     */
    public int getPiece (int col, int row)
    {
        try {
            return _board[(row*_bwid) + col];
        } catch (Exception e) {
            Log.warning("Failed getting piece [col=" + col +
                        ", row=" + row + ", error=" + e + "].");
            Log.logStackTrace(e);
            return -1;
        }
    }

    /**
     * For boards that are always filled, this method is called to obtain
     * pieces to fill the board.
     */
    public int getNextPiece ()
    {
        return PIECE_NONE;
    }

    /**
     * Returns the distance the piece at the given column and row can drop
     * until it hits a non-empty piece (defined as {@link #PIECE_NONE}).
     */
    public int getDropDistance (int col, int row)
    {
	int dist = 0;
	for (int yy = row + 1; yy < _bhei; yy++) {
	    if (getPiece(col, yy) != PIECE_NONE) {
		return dist;
	    }
	    dist++;
	}
	return dist;
    }

    /**
     * Returns whether the given row in the board is empty.
     */
    public boolean isRowEmpty (int row)
    {
        for (int col = 0; col < _bwid; col++) {
            if (getPiece(col, row) != PIECE_NONE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether all of the pieces at the given coordinates can be
     * dropped one row.
     */
    public boolean isValidDrop (int[] rows, int[] cols, float pctdone)
    {
        int bottom = _bhei - 1;
        for (int ii = 0; ii < rows.length; ii++) {
            // pieces at bottom can't be dropped
            if (rows[ii] >= bottom) {
                return false;
            }

            // pieces with pieces below them can't be dropped
            int row = rows[ii] + 1;
            if (row >= 0 && getPiece(cols[ii], row) != PIECE_NONE) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the specified coordinate is within the bounds of
     * the board, false if it is not.
     */
    public boolean inBounds (int col, int row)
    {
        return (col >= 0 && row >= 0 && col < getWidth() && row < getHeight());
    }

    /**
     * Returns whether the specified block in the board is empty.  The
     * block is allowed to occupy space off the top of the board as long
     * as it is within the horizontal board bounds.
     *
     * @param col the left coordinate of the block.
     * @param row the bottom coordinate of the block.
     * @param wid the width of the block.
     * @param hei the height of the block.
     */
    public boolean isBlockEmpty (int col, int row, int wid, int hei)
    {
        for (int ypos = row; ypos > (row - hei); ypos--) {
            for (int xpos = col; xpos < (col + wid); xpos++) {
                // only allow movement off the top of the board that's
                // within the horizontal screen bounds and in a column
                // that's not topped out
                if (ypos < 0) {
                    if ((xpos < 0 || xpos >= _bwid) ||
                        (getPiece(xpos, 0) != PIECE_NONE)) {
                        return false;
                    } else {
                        continue;
                    }
                }

                // don't allow movement outside the side or bottom bounds
                if (xpos < 0 ||
                    xpos >= _bwid ||
                    ypos >= _bhei) {
                    return false;
                }

                // make sure no piece is present
                if (getPiece(xpos, ypos) != PIECE_NONE) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Rotates the given block in the given direction and returns its
     * final state as <code>(orient, col, row, popped)</code>, where
     * <code>orient</code> is the final orientation of the drop block;
     * <code>col</code> and <code>row</code> are the final column and row
     * coordinates, respectively, of the central drop block piece.
     * <code>popped</code> will be set to 1 if the piece was popped up, 0
     * otherwise.
     */
    public int[] getForgivingRotation (
        int[] rows, int[] cols, int orient, int dir, int rtype, float pctdone,
        boolean canPopup)
    {
        int px = cols[0], py = rows[0];

//         Log.info("Starting rotation [px=" + px + ", py=" + py +
//                  ", orient=" + orient + ", pctdone=" + pctdone + "].");

        // try rotating the block in the given direction through all four
        // possible orientations
        for (int ii = 0; ii < 4; ii++) {
            int oidx = orient/2;

            // adjust the position of the central piece
            px += ROTATE_DX[rtype][dir][oidx];
            py += ROTATE_DY[rtype][dir][oidx];

            // update the orientation
            orient = DropBoardUtil.getRotatedOrientation(orient, dir);
            oidx = orient/2;

            // because isBlockEmpty() always assumes the origin of the
            // block is in the lower-left, we need to adjust the
            // coordinates of the drop block's "central" piece accordingly
            int ox = px + ORIENT_ORIGIN_DX[oidx];
            int oy = py + ORIENT_ORIGIN_DY[oidx];

            // if we're less than 50 percent through with our fall, we
            // want to check our current coordinates for validity; if
            // we're more, we want to check the row below our current
            // coordinates
            if (pctdone > 0.5) {
                oy += 1;
            }

            // try each of three coercions: nothing, one left, one right
            for (int c = 0; c < COERCE_DX.length; c++) {
                int cx = COERCE_DX[c];
                // check if our hypothetical new coordinates are empty
                if (isBlockEmpty(ox + cx, oy,
                                 ORIENT_WIDTHS[oidx], ORIENT_HEIGHTS[oidx])) {
//                     Log.info(
//                         "Block is empty [ox=" + ox + ", cx=" + cx +
//                         ", oy=" + oy + ", oidx=" + oidx +
//                         ", orient=" + DirectionUtil.toShortString(orient) +
//                         ", owid=" + ORIENT_WIDTHS[oidx] +
//                         ", ohei=" + ORIENT_HEIGHTS[oidx] + "].");
                    return new int[] { orient, px + cx, py, 0 };
                }
            }

            // if our piece is facing south and we're using radial
            // rotation then we need to try popping the piece up a row to
            // check for a fit
            if (canPopup && rtype == RADIAL_ROTATION && orient == SOUTH) {
                // check if our hypothetical new coordinates are empty
                if (isBlockEmpty(ox, oy - 1,
                                 ORIENT_WIDTHS[oidx], ORIENT_HEIGHTS[oidx])) {
//                     Log.info(
//                         "Popped-up block is empty [ox=" + ox +
//                         ", oy=" + (oy - 1) + ", oidx=" + oidx +
//                         ", orient=" + DirectionUtil.toShortString(orient) +
//                         ", owid=" + ORIENT_WIDTHS[oidx] +
//                         ", ohei=" + ORIENT_HEIGHTS[oidx] +
//                         ", bhei=" + _bhei + "].");
                    return new int[] { orient, px, py - 1, 1 };
                }
            }
        }

        // this should never happen since even in the most tightly
        // constrained case where the block is entirely surrounded by
        // other pieces there are always two valid orientations.
        Log.warning("**** We're horked and couldn't rotate at all!");
//         System.exit(0);
        return null;
    }

    /**
     * Returns a {@link Point} object containing the coordinates to place
     * the bottom-left of the given block at after moving it the given
     * distance on the x- and y-axes, or <code>null</code> if the move is
     * not valid.  Note that only the final block position is checked.
     *
     * @param col the leftmost column of the block.
     * @param row the bottommost row of the block.
     * @param wid the width of the block.
     * @param hei the height of the block.
     * @param dx the distance to move the block in columns.
     * @param dy the distance to move the block in rows.
     * @param pctdone the percentage of the inter-block distance that the
     * piece has fallen thus far.
     */
    public Point getForgivingMove (
        int col, int row, int wid, int hei, int dx, int dy, float pctdone)
    {
        // try placing the block in the desired position and, failing
        // that, at the same horizontal position but one row farther down
        int xpos = col + dx, ypos = row + dy;

        // if we're above the halfway mark, we check our current neighbors
        // to see if we can move there; if we're below the halfway mark we
        // check the next row down
        if (pctdone >= 0.5) {
            ypos += 1;
        }

        // if the block we wish to occupy is empty, we're all good
        return (isBlockEmpty(xpos, ypos, wid, hei)) ?
            new Point(xpos, row + dy) : null;
    }

    /**
     * Populates the given array with the column levels for this board.
     */
    public void getColumnLevels (byte[] columns)
    {
        int bwid = getWidth(), bhei = getHeight();
        for (int col = 0; col < bwid; col++) {
            int dist = getDropDistance(col, -1);
            columns[col] = (byte)(bhei - dist);
        }
    }

    /**
     * Called by the {@link DropControllerDelegate} when it's time to
     * apply a rising row of pieces to the board.  Shifts all of the
     * pieces in the given board up one row and places the given row of
     * pieces at the bottom of the board.
     */
    public void applyRisingPieces (int[] pieces)
    {
        // shift all pieces up one row
        int end = _bhei - 1;
        for (int yy = 0; yy < end; yy++) {
            for (int xx = 0; xx < _bwid; xx++) {
                setPiece(xx, yy, getPiece(xx, yy + 1));
            }
        }

        // apply the row pieces to the board
        int ypos = _bhei - 1;
        for (int xx = 0; xx < _bwid; xx++) {
            setPiece(xx, ypos, pieces[xx]);
        }
    }

    /**
     * Returns true if the specified row (which count down, with zero at
     * the top of the board) contains any pieces.
     *
     * @param row the row to check for pieces.
     * @param blankPiece the blank piece value, non-instances of which
     * will be sought.
     */
    public boolean rowContainsPieces (int row, int blankPiece)
    {
        for (int x = 0; x < _bwid; x++) {
            if (getPiece(x, row) != blankPiece) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fills the board contents with the given piece.
     */
    public void fill (int piece)
    {
        Arrays.fill(_board, (int)piece);
    }

    /**
     * Sets the piece at the given coordinates.
     *
     * @return true if the piece was set, false if it was invalid.
     */
    public boolean setPiece (int col, int row, int piece)
    {
        if (col >= 0 && row >= 0 && col < _bwid && row < _bhei) {
            _board[(row*_bwid) + col] = (int)piece;
            return true;

        } else {
            Log.warning("Attempt to set piece outside board bounds " +
                        "[col=" + col + ", row=" + row + ", p=" + piece + "].");
            return false;
        }
    }

    /**
     * Sets the pieces within the specified rectangle to the given piece.
     */
    public void setRect (int x, int y, int width, int height, int piece)
    {
        for (int yy = y; yy > (y - height); yy--) {
            for (int xx = x; xx < (x + width); xx++) {
                setPiece(xx, yy, piece);
            }
        }
    }

    /**
     * Sets the pieces in the given board segment to the specified piece.
     *
     * @param dir the direction of the segment; one of {@link #HORIZONTAL}
     * or {@link #VERTICAL}.
     * @param col the starting column of the segment.
     * @param row the starting row of the segment.
     * @param len the length of the segment in pieces.
     * @param piece the piece to set in the segment.
     *
     * @return false if the segment was only partially applied because
     * some pieces were outside the bounds of the board, true if it was
     * completely applied.
     */
    public boolean setSegment (int dir, int col, int row, int len, int piece)
    {
        _setPieceOp.init(piece);
        applyOp(dir, col, row, len, _setPieceOp);
        return !_setPieceOp.getError();
    }

    /**
     * Sets the pieces in the given board segment to the specified pieces.
     *
     * @param dir the direction of the segment; one of {@link #HORIZONTAL}
     * or {@link #VERTICAL}.
     * @param col the starting column of the segment.
     * @param row the starting row of the segment.
     * @param pieces the pieces to set in the segment.
     */
    public void setSegment (int dir, int col, int row, int[] pieces)
    {
        _setSegmentOp.init(dir, pieces);
        applyOp(dir, col, row, pieces.length, _setSegmentOp);
    }

    /**
     * Applies a specified {@link PieceOperation} to all pieces in the
     * specified row or column starting at the specified coordinates and
     * spanning the remainder of the row or column (depending on the
     * application direction) in the board.
     *
     * @param dir the direction to iterate in; one of {@link #HORIZONTAL}
     * or {@link #VERTICAL}.
     * @param col the starting column of the segment.
     * @param row the starting row of the segment.
     * @param op the piece operation to apply to each piece.
     */
    public void applyOp (int dir, int col, int row, PieceOperation op)
    {
        int len = (dir == HORIZONTAL) ? _bwid - col : row + 1;
        applyOp(dir, col, row, len, op);
    }

    /**
     * Applies a specified {@link PieceOperation} to all pieces in a row
     * or column segment starting at the specified coordinates and of the
     * specified length in the board.
     *
     * @param dir the direction to iterate in; one of {@link #HORIZONTAL}
     * or {@link #VERTICAL}.
     * @param col the starting leftmost column of the segment.
     * @param row the starting bottommost row of the segment.
     * @param len the number of pieces in the segment.
     * @param op the piece operation to apply to each piece.
     */
    public void applyOp (int dir, int col, int row, int len, PieceOperation op)
    {
        if (dir == HORIZONTAL) {
            int end = Math.min(col + len, _bwid);
            for (int ii = col; ii < end; ii++) {
                if (!op.execute(this, ii, row)) {
                    break;
                }
            }

        } else {
            int end = Math.max(row - len, -1);
            for (int ii = row; ii > end; ii--) {
                if (!op.execute(this, col, ii)) {
                    break;
                }
            }
        }
    }

    /**
     * Applies a specified {@link PieceOperation} to the specified piece
     * in the board.
     *
     * @param col the column of the piece.
     * @param row the row of the piece.
     * @param op the piece operation to apply to the piece.
     */
    public void applyOp (int col, int row, PieceOperation op)
    {
        op.execute(this, col, row);
    }

    // documentation inherited from interface
    public void dump ()
    {
        dumpAndCompare(null);
    }

    // documentation inherited from interface
    public void dumpAndCompare (Board other)
    {
        if (other != null && !(other instanceof DropBoard)) {
            throw new IllegalArgumentException(
                "Can't compare drop board to non-drop-board.");
        }

        DropBoard dother = (DropBoard)other;
        int padwid = getPadWidth();
        if (other != null) {
            // padwid = (padwid * 2) + 1;
            padwid *= 2;
        }

	for (int y = 0; y < _bhei; y++) {
	    StringBuffer buf = new StringBuffer();
	    for (int x = 0; x < _bwid; x++) {
                int piece = getPiece(x, y);
                String str = formatPiece(piece);
                if (dother != null) {
                    int opiece = dother.getPiece(x, y);
                    if (opiece != piece) {
                        str += "|" + formatPiece(opiece);
                    }
                }
                buf.append(StringUtils.rightPad(str, padwid));
	    }
	    Log.warning(buf.toString());
	}
    }

    /** Returns a string representation of this instance. */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[wid=").append(_bwid);
        buf.append(", hei=").append(_bhei);
        return buf.append("]").toString();
    }

    // documentation inherited from interface
    public boolean equals (Board other)
    {
        // make sure we're comparing the same class type
        if (!this.getClass().getName().equals(other.getClass().getName())) {
            throw new IllegalArgumentException(
                "Can't compare board of different class types " +
                "[src=" + this.getClass().getName() +
                ", other=" + other.getClass().getName() + "].");
        }

        // we're certainly not equal if our dimensions differ
        DropBoard dother = (DropBoard)other;
        if (dother.getWidth() != _bwid ||
            dother.getHeight() != _bhei) {
            return false;
        }

        // check each board piece
        for (int xx = 0; xx < _bwid; xx++) {
            for (int yy = 0; yy < _bhei; yy++) {
                if (getPiece(xx, yy) != dother.getPiece(xx, yy)) {
                    return false;
                }
            }
        }

        // we're equal
        return true;
    }

    /**
     * Returns whether the given coordinates are within the board bounds.
     */
    public boolean isValidPosition (int x, int y)
    {
        return (x >= 0 &&
                y >= 0 &&
                x < _bwid &&
                y < _bhei);
    }

    /**
     * Returns the bounds of this board.  Note that a single rectangle is
     * re-used internally and so the caller should not modify the
     * returned rectangle.
     */
    public Rectangle getBounds ()
    {
        if (_bounds == null) {
            _bounds = new Rectangle(0, 0, _bwid, _bhei);
        }
        return _bounds;
    }

    /**
     * Returns the size of the board in pieces.
     */
    public int size ()
    {
        return (_bwid*_bhei);
    }

    /**
     * Copies the contents of this board directly into the supplied board,
     * overwriting the destination board in its entirety.
     */
    public void copyInto (DropBoard board)
    {
        // make sure the target board is a valid target
        if (board.getWidth() != _bwid || board.getHeight() != _bhei) {
            Log.warning("Can't copy board into destination board with " +
                        "different dimensions [src=" + this +
                        ", dest=" + board + "].");
            return;
        }

        // copy our pieces directly into the board, avoiding any unsightly
        // object allocation which is largely the point of this method,
        // after all.
        int[] dest = ((DropBoard)board).getBoard();
        System.arraycopy(_board, 0, dest, 0, (_bwid*_bhei));
    }

    /**
     * Returns the raw board data associated with this board.  One
     * shouldn't fiddle about with this unless one knows what one is
     * doing.
     */
    public int[] getBoard ()
    {
        return _board;
    }

    /**
     * Sets the board data and board dimensions.
     */
    public void setBoard (int[] board, int bwid, int bhei)
    {
        _board = board;
        _bwid = bwid;
        _bhei = bhei;
    }

    /**
     * Sets the board pieces.
     */
    public void setBoard (int[] board)
    {
        int size = (_bwid*_bhei);
        if (board.length < size) {
            Log.warning("Attempt to set board with invalid data size " +
                        "[len=" + board.length + ", expected=" + size + "].");
            return;
        }

        _board = board;
    }

    // documentation inherited
    public Object clone ()
    {
        DropBoard board = (DropBoard)super.clone();
        board._board = new int[_board.length];
        System.arraycopy(_board, 0, board._board, 0, _board.length);
        return board;
    }

    /**
     * Returns the number of characters to which a single piece should be
     * padded when dumping the board for debugging purposes.
     */
    protected int getPadWidth ()
    {
        return DEFAULT_PAD_WIDTH;
    }

    /**
     * Returns a string representation of the given piece for use when
     * dumping the board.
     */
    protected String formatPiece (int piece)
    {
        return (piece == PIECE_NONE) ? "." : String.valueOf(piece);
    }

    /** An operation that sets the pieces in a board segment to a
     * specified array of pieces. */
    protected static class SetSegmentOperation implements PieceOperation
    {
        /**
         * Sets the array of pieces to be placed in the board segment.
         */
        public void init (int dir, int[] pieces)
        {
            _dir = dir;
            _pieces = pieces;
            _idx = (dir == HORIZONTAL) ? _pieces.length - 1 : 0;
        }

        // documentation inherited
        public boolean execute (DropBoard board, int col, int row)
        {
            if (_dir == HORIZONTAL) {
                board.setPiece(col, row, _pieces[_idx--]);
            } else {
                board.setPiece(col, row, _pieces[_idx++]);
            }
            return true;
        }

        /** The orientation in which the pieces are to be placed. */
        protected int _dir;

        /** The current piece index. */
        protected int _idx;

        /** The pieces to set in the board. */
        protected int[] _pieces;
    }

    /** An operation that sets all pieces to a specified piece. */
    protected static class SetPieceOperation implements PieceOperation
    {
        /**
         * Sets the piece to be placed in the board segment.
         */
        public void init (int piece)
        {
            _piece = piece;
            _error = false;
        }

        /**
         * Returns true if we attempted to set a piece outside the bounds
         * of the board during the course of our operation.
         */
        public boolean getError ()
        {
            return _error;
        }

        // documentation inherited
        public boolean execute (DropBoard board, int col, int row)
        {
            if (!board.setPiece(col, row, _piece)) {
                _error = true;
            }
            return true;
        }

        /** The piece to set in the board. */
        protected int _piece;

        /** Set to true if an error occurred setting a piece. */
        protected boolean _error;
    }

    /** The board data. */
    protected int[] _board;

    /** The board dimensions in pieces. */
    protected int _bwid, _bhei;

    /** The bounds of this board. */
    protected transient Rectangle _bounds;

    // used to reconfigure the block when rotating it
    protected static final int[][][] ROTATE_DX = {
        //  W  N  E  S       W  N  E  S
        {{  0, 0, 0, 0 }, {  0, 0, 0, 0 }}, // RADIAL
        {{ -1, 1, 0, 0 }, { -1, 0, 0, 1 }}, // INPLACE
        //     CCW              CW
    };

    // used to reconfigure the block when rotating it
    protected static final int[][][] ROTATE_DY = {
        //  W  N  E  S      W  N   E  S
        {{  0, 0, 0, 0 }, { 0, 0,  0, 0 }}, // RADIAL
        {{ -1, 0, 0, 1 }, { 0, 0, -1, 1 }}, // INPLACE
        //     CCW              CW
    };

    // used to compute the bounds of the isBlockEmpty() block based on the
    // drop block's orientation and "root" block position
    protected static final int[] ORIENT_WIDTHS = { 2, 1, 2, 1 };
    protected static final int[] ORIENT_HEIGHTS = { 1, 2, 1, 2 };

    // used to compute the origin of the isBlockEmpty() block based on the
    // drop block's orientation and "root" block position
    protected static final int[] ORIENT_ORIGIN_DX = { -1, 0, 0, 0 };
    protected static final int[] ORIENT_ORIGIN_DY = { 0, 0, 0, 1 };

    // used to coerce the block when rotating either a space to the left
    // or right (or not at all)
    protected static final int[] COERCE_DX = { 0, 1, -1 };

    /** The operation used to set the pieces in a board segment. */
    protected static final SetSegmentOperation _setSegmentOp =
        new SetSegmentOperation();

    /** The operation used to set a piece in a board segment. */
    protected static final SetPieceOperation _setPieceOp =
        new SetPieceOperation();

    /** The number of characters to which each board piece should be
     * padded when outputting for debug purposes. */
    protected static final int DEFAULT_PAD_WIDTH = 3;
}
