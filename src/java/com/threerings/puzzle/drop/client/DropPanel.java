//
// $Id: DropPanel.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.client;

import com.threerings.puzzle.data.BoardSummary;

/**
 * Puzzles using the drop services need implement this interface to
 * display drop puzzle related information.
 */
public interface DropPanel
{
    /**
     * Sets the next block to be displayed.
     */
    public void setNextBlock (int[] pieces);

    /**
     * Updates the board summary display for the given player.
     */
    public void setSummary (int pidx, BoardSummary summary);
}
