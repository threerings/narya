//
// $Id: BaseTile.java,v 1.4 2002/02/06 17:13:06 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Image;
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
    public BaseTile (Image image)
    {
        super(image);
    }

    /**
     * Constructs a new base tile with the specified passability.
     */
    public BaseTile (Image image, boolean passable)
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
        return _passable && !_covered;
    }

    /**
     * Sets whether or not this tile can be walked upon by character
     * sprites.
     */
    public void setPassable (boolean passable)
    {
        _passable = passable;
    }

    /**
     * Sets whether this tile is currently covered by the footprint of an
     * object tile or not. When it is covered, it can't be walked on by
     * character sprites.
     */
    public void setCovered (boolean covered)
    {
        _covered = covered;
    }

    /** Whether the tile is passable. */
    protected boolean _passable = true;

    /** Whether the tile is covered by an object tile. */
    protected boolean _covered = true;
}
