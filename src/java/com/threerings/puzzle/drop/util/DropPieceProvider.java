//
// $Id: DropPieceProvider.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.util;

/**
 * Does something extraordinary.
 */
public interface DropPieceProvider
{
    /**
     * Get the next piece to add to the drop board.
     */
    public int getNextPiece ()
        throws OutOfPiecesException;

    /**
     * I lost my touch.
     */
    public static class OutOfPiecesException extends Exception
    {
    }
}
