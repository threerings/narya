//
// $Id: ByteDropBoard.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

import java.util.Arrays;

import com.threerings.puzzle.Log;

/**
 * The byte drop board extends the {@link DropBoard}, making use of a
 * <code>byte</code> array of pieces to store the board contents.
 */
public class ByteDropBoard extends DropBoard
{
    /**
     * Constructs an empty byte drop board for use when unserializing.
     */
    public ByteDropBoard ()
    {
        this(null, 0, 0);
    }

    /**
     * Constructs a byte drop board of the given dimensions with its
     * pieces initialized to 0.
     */
    public ByteDropBoard (int bwid, int bhei)
    {
        this(new byte[bwid*bhei], bwid, bhei);
        fill(PIECE_NONE);
    }

    /**
     * Constructs a byte drop board of the given dimensions with its
     * pieces initialized to the given piece.
     */
    public ByteDropBoard (int bwid, int bhei, byte piece)
    {
        this(new byte[bwid*bhei], bwid, bhei);
        fill(piece);
    }

    /**
     * Constructs a byte drop board with the given board and dimensions.
     */
    public ByteDropBoard (byte[] board, int bwid, int bhei)
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
        Arrays.fill(_board, (byte)piece);
    }

    /**
     * Sets the board data and board dimensions.
     */
    public void setBoard (byte[] board, int bwid, int bhei)
    {
        _board = board;
        _bwid = bwid;
        _bhei = bhei;
    }

    /**
     * Sets the board pieces.
     */
    public void setBoard (byte[] board)
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
            _board[(row*_bwid) + col] = (byte)piece;
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
        byte[] dest = ((ByteDropBoard)board).getBoard();
        System.arraycopy(_board, 0, dest, 0, (_bwid*_bhei));
    }

    // documentation inherited
    public Object clone ()
    {
        ByteDropBoard board = (ByteDropBoard)super.clone();
        int size = _bwid*_bhei;
        board._board = new byte[size];
        System.arraycopy(_board, 0, board._board, 0, size);
        return board;
    }

    /**
     * Returns the raw board data associated with this board.  One
     * shouldn't fiddle about with this unless one knows what one is
     * doing.
     */
    public byte[] getBoard ()
    {
        return _board;
    }

    /** The board data. */
    protected byte[] _board;
}
