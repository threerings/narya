//
// $Id: DropLogic.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

/**
 * Describes the features and configuration desired for a given drop
 * puzzle game.
 */
public interface DropLogic
{
    /**
     * Returns whether the puzzle game would like to make use of the
     * manipulable block dropping functionality.
     */
    public boolean useBlockDropping ();

    /**
     * Returns whether the puzzle game would like to make use of the
     * rising board functionality.
     */
    public boolean useBoardRising ();
}
