//
// $Id: PuzzleConfig.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.data;

import com.threerings.parlor.game.GameConfig;

/**
 * Encapsulates the basic configuration information for a puzzle game.
 */
public abstract class PuzzleConfig extends GameConfig
    implements Cloneable
{
    /**
     * Returns a translatable label describing this puzzle.
     */
    public abstract String getPuzzleName ();

    /**
     * Returns the puzzle rating type.
     */
    public abstract byte getRatingTypeId ();

    /**
     * Constructs a blank puzzle config.
     */
    public PuzzleConfig ()
    {
    }

    /**
     * If this method returns true, a copy of the client board will be
     * sent with every puzzle event so that the server can compare them
     * step-by-step to debug out of sync problems.
     */
    public boolean syncBoardState ()
    {
        return PuzzleCodes.SYNC_BOARD_STATE;
    }

    /**
     * Returns the message bundle identifier for the bundle that should be
     * used to translate the translatable strings used to describe the
     * puzzle config parameters.  The default implementation returns the
     * base puzzle message bundle, but puzzles that have their own message
     * bundle should override this method and return their puzzle-specific
     * bundle identifier.
     */
    public String getBundleName ()
    {
        return PuzzleCodes.PUZZLE_MESSAGE_BUNDLE;
    }

    /**
     * Returns a clone of this puzzle config.
     */
    public PuzzleConfig getClone ()
    {
        try {
            return (PuzzleConfig)clone();
        } catch (CloneNotSupportedException cnse) {
            // something has gone horribly awry
            return null;
        }
    }
}
