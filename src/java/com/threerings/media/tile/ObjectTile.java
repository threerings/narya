//
// $Id: ObjectTile.java,v 1.4 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;

/**
 * An object tile extends the base tile to provide support for objects
 * whose image spans more than one unit tile.  An object tile has
 * dimensions (in tile units) that represent its footprint or "shadow".
 */
public class ObjectTile extends Tile
{
    /**
     * Constructs a new object tile with the specified image. The base
     * width and height should be set before using this tile.
     */
    public ObjectTile (Image image)
    {
        super(image);
    }

    /**
     * Constructs a new object tile with the specified base width and
     * height.
     */
    public ObjectTile (Image image, int baseWidth, int baseHeight)
    {
	super(image);
	_baseWidth = baseWidth;
	_baseHeight = baseHeight;
    }

    /**
     * Returns the object footprint width in tile units.
     */
    public int getBaseWidth ()
    {
        return _baseWidth;
    }

    /**
     * Returns the object footprint height in tile units.
     */
    public int getBaseHeight ()
    {
        return _baseHeight;
    }

    /**
     * Sets the object footprint width in tile units.
     */
    public void setBaseWidth (int baseWidth)
    {
        _baseWidth = baseWidth;
    }

    /**
     * Sets the object footprint height in tile units.
     */
    public void setBaseHeight (int baseHeight)
    {
        _baseHeight = baseHeight;
    }

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", baseWidth=").append(_baseWidth);
        buf.append(", baseHeight=").append(_baseHeight);
    }

    /** The object footprint dimensions in unit tile units. */
    protected int _baseWidth, _baseHeight;
}
