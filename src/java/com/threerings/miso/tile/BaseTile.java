//
// $Id: BaseTile.java,v 1.2 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Image;
import com.threerings.media.tile.Tile;

/**
 * Extends the base tile class to add support for tile passability.
 *
 * @see MisoTileSet
 */
public class MisoTile extends Tile
{
    /**
     * Constructs a new miso tile with the specified image. Passability
     * will be assumed to be true.
     */
    public MisoTile (Image image)
    {
        super(image);
    }

    /**
     * Constructs a new miso tile with the specified passability.
     */
    public MisoTile (Image image, boolean passable)
    {
	super(image);
	_passable = passable;
    }

    /**
     * Returns whether or not this tile can be walked upon by character
     * sprites.
     */
    public boolean isPassable ()
    {
        return _passable;
    }

    /**
     * Sets whether or not this tile can be walked upon by character
     * sprites.
     */
    public void setPassable (boolean passable)
    {
        _passable = passable;
    }

    /** Whether the tile is passable. */
    protected boolean _passable = true;
}
