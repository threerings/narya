//
// $Id: PuzzleManagerDelegate.java,v 1.3 2004/06/22 14:08:58 mdb Exp $

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
        _puzmgr = puzmgr;
    }

    protected PuzzleManager _puzmgr;
}
