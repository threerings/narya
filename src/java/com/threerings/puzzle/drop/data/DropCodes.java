//
// $Id: DropCodes.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

import com.threerings.puzzle.data.PuzzleGameCodes;

/**
 * Contains codes used by the drop game services.
 */
public interface DropCodes extends PuzzleGameCodes
{
    /** The message bundle identifier for drop puzzle messages. */
    public static final String DROP_MESSAGE_BUNDLE = "puzzle.drop";

    /** The name of the control stream that provides drop pieces. */
    public static final String DROP_STREAM = "drop";

    /** The name of the control stream that provides rise pieces. */
    public static final String RISE_STREAM = "rise";
}
