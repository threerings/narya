//
// $Id: PuzzlerObject.java,v 1.2 2004/02/25 14:48:44 mdb Exp $

package com.threerings.puzzle.data;

/**
 * An interface that must be implemented by {@link BodyObject} derivations
 * that wish to be usable with the puzzle services.
 */
public interface PuzzlerObject
{
    /**
     * Returns this puzzler's "puzzle location which is the oid of the
     * puzzle game object. Should return -1 until some value is set via
     * {@link #setPuzzleLoc}.
     */
    public int getPuzzleLoc ();

    /**
     * Sets this puzzler's "puzzle location".
     */
    public void setPuzzleLoc (int puzzleLoc);
}
