//
// $Id: DropSpriteObserver.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.client;

/**
 * Provides notifications for drop puzzle specific stuff.
 */
public interface DropSpriteObserver
{
    /**
     * Called when the drop sprite has moved completely to the specified
     * board coordinates.
     */
    public void pieceMoved (DropSprite sprite, long when, int col, int row);
}
