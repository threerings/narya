//
// $Id: PuzzleAnimationWaiter.java,v 1.2 2004/02/25 14:48:44 mdb Exp $

package com.threerings.puzzle.client;

import com.threerings.media.animation.AnimationWaiter;

import com.threerings.puzzle.data.PuzzleObject;

/**
 * An animation waiter to be used with puzzles that want to modify the
 * game object or board in some way after the animations end, and would
 * like to do so in a safe fashion such that their changes aren't
 * unwittingly performed on game data for a subsequent round of the
 * puzzle.
 */
public abstract class PuzzleAnimationWaiter extends AnimationWaiter
{
    /**
     * Constructs a puzzle animation waiter.
     */
    public PuzzleAnimationWaiter (PuzzleObject puzobj)
    {
        _puzobj = puzobj;
        _roundId = puzobj.roundId;
    }

    /**
     * Returns whether the puzzle associated with this puzzle animation
     * waiter is still valid.
     */
    public boolean puzzleStillValid ()
    {
        return (_puzobj.isInPlay() && (_roundId == _puzobj.roundId));
    }

    // documentation inherited
    protected final void allAnimationsFinished ()
    {
        allAnimationsFinished(puzzleStillValid());
    }

    /**
     * Replacement for {@link AnimationWaiter#allAnimationsFinished} that
     * also reports whether the puzzle associated with this animation
     * waiter is still valid.
     */
    protected abstract void allAnimationsFinished (boolean puzStillValid);

    /** The initial round id. */
    protected int _roundId;

    /** The puzzle object that the animations we're observering want to
     * modify. */
    protected PuzzleObject _puzobj;
}
