//
// $Id: ShortDropBoard.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

import java.util.Arrays;

import com.threerings.puzzle.Log;

/**
 * The short drop board extends the {@link DropBoard}, making use of a
 * <code>short</code> array of pieces to store the board contents.
 */
public class ShortDropBoard extends DropBoard
{
    /**
     * Constructs an empty short drop board for use when unserializing.
     */
    public ShortDropBoard ()
    {
        this(null, 0, 0);
    }

    /**
     * Constructs a short drop board of the given dimensions with its
     * pieces initialized to PIECE_NONE.
     */
    public ShortDropBoard (int bwid, int bhei)
    {
        this(new short[bwid*bhei], bwid, bhei);
        fill(PIECE_NONE);
    }

    /**
     * Constructs a short drop board of the given dimensions with its
     * pieces initialized to the given piece.
     */
    public ShortDropBoard (int bwid, int bhei, short piece)
    {
        this(new short[bwid*bhei], bwid, bhei);
        fill(piece);
    }

    /**
     * Constructs a short drop board with the given board and dimensions.
     */
    public ShortDropBoard (short[] board, int bwid, int bhei)
    {
        _board = board;
        _bwid = bwid;
        _bhei = bhei;
    }

    // documentation inherited from interface
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

    // documentation inherited from interface
    public void fill (int piece)
    {
        Arrays.fill(_board, (short)piece);
    }

    /**
     * Sets the board data and board dimensions.
     */
    public void setBoard (short[] board, int bwid, int bhei)
    {
        _board = board;
        _bwid = bwid;
        _bhei = bhei;
    }

    /**
     * Sets the board pieces.
     */
    public void setBoard (short[] board)
    {
        int size = (_bwid*_bhei);
        if (board.length < size) {
            Log.warning("Attempt to set board with invalid data size " +
                        "[len=" + board.length + ", expected=" + size + "].");
            return;
        }

        _board = board;
    }

    // documentation inherited from interface
    public boolean setPiece (int col, int row, int piece)
    {
        if (col >= 0 && row >= 0 && col < _bwid && row < _bhei) {
            _board[(row*_bwid) + col] = (short)piece;
            return true;

        } else {
            Log.warning("Attempt to set piece outside board bounds " +
                        "[col=" + col + ", row=" + row + ", p=" + piece + "].");
            return false;
        }
    }

    // documentation inherited from interface
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
        short[] dest = ((ShortDropBoard)board).getBoard();
        System.arraycopy(_board, 0, dest, 0, (_bwid*_bhei));
    }

    // documentation inherited
    public Object clone ()
    {
        int size = _bwid*_bhei;
        short[] data = new short[size];
        System.arraycopy(_board, 0, data, 0, size);
        return new ShortDropBoard(data, _bwid, _bhei);
    }

    /**
     * Returns the raw board data associated with this board.  One
     * shouldn't fiddle about with this unless one knows what one is
     * doing.
     */
    public short[] getBoard ()
    {
        return _board;
    }

    /** The board data. */
    protected short[] _board;
}
