//
// $Id: DropBoardSummary.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.BoardSummary;

/**
 * Provides a summary of a {@link DropBoard}.
 */
public class DropBoardSummary extends BoardSummary
{
    /** The row levels for each column. */
    public byte[] columns;

    /**
     * Constructs an empty drop board summary for use when un-serializing.
     */
    public DropBoardSummary ()
    {
        // nothing for now
    }

    /**
     * Constructs a drop board summary that retrieves board information
     * from the supplied board when summarizing.
     */
    public DropBoardSummary (Board board)
    {
        super(board);

        // create the columns array
        columns = new byte[_dboard.getWidth()];
    }

    /**
     * Returns the column number of the column within the given column
     * range that contains the most pieces.
     */
    public int getHighestColumn (int startx, int endx)
    {
        byte value = columns[startx];
        int idx = startx;
        for (int xx = startx + 1; xx <= endx; xx++) {
            if (columns[xx] > value) {
                value = columns[xx];
                idx = xx;
            }
        }
        return idx;
    }

    // documentation inherited
    public void setBoard (Board board)
    {
        super.setBoard(board);

        _dboard = (DropBoard)board;
    }

    // documentation inherited
    public void summarize ()
    {
        // update the board column levels
        _dboard.getColumnLevels(columns);
    }

    /** The drop board we're summarizing. */
    protected transient DropBoard _dboard;
}
