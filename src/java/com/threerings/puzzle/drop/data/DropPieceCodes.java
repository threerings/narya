//
// $Id: DropPieceCodes.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

import com.threerings.util.DirectionCodes;

/**
 * The drop piece codes interface contains constants common to the drop
 * game package.
 */
public interface DropPieceCodes extends DirectionCodes
{
    /** The piece constant denoting an empty board piece. */
    public static final byte PIECE_NONE = -1;

    /** The number of pieces in a drop block. */
    public static final int DROP_BLOCK_PIECE_COUNT = 2;
}
