//
// $Id: PuzzleAnimationWaiter.java,v 1.3 2004/08/27 02:20:27 mdb Exp $
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
