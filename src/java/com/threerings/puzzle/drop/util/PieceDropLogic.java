//
// $Id: PieceDropLogic.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.util;

import com.threerings.util.DirectionCodes;
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
