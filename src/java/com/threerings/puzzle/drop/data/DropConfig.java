//
// $Id: DropConfig.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.data;

/**
 * Provides access to the configuration information for a drop puzzle
 * game.
 */
public interface DropConfig
{
    /** Returns the board width in pieces. */
    public int getBoardWidth ();

    /** Returns the board height in pieces. */
    public int getBoardHeight ();
}
