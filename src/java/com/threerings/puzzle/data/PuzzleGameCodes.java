//
// $Id: PuzzleGameCodes.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.data;

/**
 * Contains codes used by the puzzle game client implementations. This
 * differs from {@link PuzzleCodes} as that is related to the puzzle
 * services which span the client and the server.
 */
public interface PuzzleGameCodes
{
    /** Enable this flag to test the game with a robot player. */
    public static final boolean ROBOT_TEST = false;
}
