//
// $Id: PieceDropLogic.java,v 1.3 2004/08/27 02:20:31 mdb Exp $
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

import com.threerings.puzzle.drop.data.DropBoard;

/**
 * An interface to be implemented by games that would like to be able to
 * drop their pieces during game play.
 */
public interface PieceDropLogic
{
    /**
     * Should the board always be filled?
     *
     * @return false for normal behavior.
     */
    public boolean boardAlwaysFilled ();

    /**
     * Returns whether the given piece is potentially droppable.
     */
    public boolean isDroppablePiece (int piece);

    /**
     * Returns whether the given piece has constraints upon it that
     * impact its droppability.
     */
    public boolean isConstrainedPiece (int piece);

    /**
     * Returns whether the given piece terminates a column climb when
     * determining the height of a piece column to be dropped.
     *
     * @param allowConst whether to allow dropping constrained pieces
     * (though only in the first encountered constrained block.)
     * @param piece the piece to consider.
     * @param pre whether the climbability check is being performed
     * before the height is incremented, or after.
     */
    public boolean isClimbablePiece (
        boolean allowConst, int piece, boolean pre);

    /**
     * Returns the x-axis coordinate of the specified edge of the
     * given constrained piece.
     * 
     * <p> TODO: This should go away once the sword and sail games
     * have standardized on WEST/EAST or BLOCK_LEFT/BLOCK_RIGHT to
     * reference block edges.
     *
     * @param board the board to search.
     * @param col the column of the constrained piece.
     * @param row the row of the constrained piece.
     * @param dir the edge direction to find; one of {@link
     * DirectionCodes#LEFT} or {@link DirectionCodes#RIGHT}.
     */
    public int getConstrainedEdge (DropBoard board, int col, int row, int dir);
}
