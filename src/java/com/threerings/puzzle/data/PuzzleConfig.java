//
// $Id: PuzzleConfig.java,v 1.2 2004/08/27 02:20:28 mdb Exp $
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
