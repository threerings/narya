//
// $Id: BaseTile.java,v 1.8 2003/05/31 00:56:38 mdb Exp $

package com.threerings.miso.tile;

import com.threerings.media.tile.Tile;

/**
 * Extends the tile class to add support for tile passability.
 *
 * @see BaseTileSet
 */
public class BaseTile extends Tile
{
    /**
     * Returns whether or not this tile can be walked upon by character
     * sprites.
     */
    public boolean isPassable ()
    {
        return _passable;
    }

    /**
     * Configures this base tile as passable or impassable.
     */
    public void setPassable (boolean passable)
    {
        _passable = passable;
    }

    /** Whether the tile is passable. */
    protected boolean _passable = true;
}
