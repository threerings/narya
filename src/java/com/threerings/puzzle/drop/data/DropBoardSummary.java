//
// $Id: DropBoardSummary.java,v 1.2 2004/08/27 02:20:30 mdb Exp $
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
