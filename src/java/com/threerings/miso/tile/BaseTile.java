//
// $Id: BaseTile.java,v 1.7 2003/01/13 22:55:12 mdb Exp $

package com.threerings.miso.tile;

import com.threerings.media.image.Mirage;
import com.threerings.media.tile.Tile;

/**
 * Extends the tile class to add support for tile passability.
 *
 * @see BaseTileSet
 */
public class BaseTile extends Tile
{
    /**
     * Constructs a new base tile with the specified image. Passability
     * will be assumed to be true.
     */
    public BaseTile (Mirage image)
    {
        this(image, true);
    }

    /**
     * Constructs a new base tile with the specified passability.
     */
    public BaseTile (Mirage image, boolean passable)
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

    /** Whether the tile is passable. */
    protected boolean _passable = true;
}
