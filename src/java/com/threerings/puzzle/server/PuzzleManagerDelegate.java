//
// $Id: PuzzleManagerDelegate.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.server;

import com.threerings.parlor.game.GameManagerDelegate;

/**
 * Extends the {@link GameManagerDelegate} mechanism with puzzle manager
 * specific methods (of which there are currently none).
 */
public class PuzzleManagerDelegate extends GameManagerDelegate
{
    /**
     * Constructs a puzzle manager delegate.
     */
    public PuzzleManagerDelegate (PuzzleManager puzmgr)
    {
        super(puzmgr);
    }

    /**
     * Called when the puzzle difficulty level is changed.
     */
    public void difficultyChanged (int level)
    {
    }
}
