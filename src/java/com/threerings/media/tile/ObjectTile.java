//
// $Id: ObjectTile.java,v 1.12 2003/01/08 04:09:02 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Rectangle;

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
    public ObjectTile (Image image, Rectangle bounds)
    {
        super(image, bounds);
    }

    /**
     * Constructs a new object tile with the specified base width and
     * height.
     */
    public ObjectTile (Image image, Rectangle bounds,
                       int baseWidth, int baseHeight)
    {
	super(image, bounds);
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
     * Sets the object footprint in tile units.
     */
    public void setBase (int width, int height)
    {
        _baseWidth = width;
        _baseHeight = height;
    }

    /**
     * Returns the x offset into the tile image of the origin (which will
     * be aligned with the bottom center of the origin tile) or
     * <code>Integer.MIN_VALUE</code> if the origin is not explicitly
     * specified and should be computed from the image size and tile
     * footprint.
     */
    public int getOriginX ()
    {
        return _originX;
    }

    /**
     * Returns the y offset into the tile image of the origin (which will
     * be aligned with the bottom center of the origin tile) or
     * <code>Integer.MIN_VALUE</code> if the origin is not explicitly
     * specified and should be computed from the image size and tile
     * footprint.
     */
    public int getOriginY ()
    {
        return _originY;
    }

    /**
     * Sets the offset in pixels from the origin of the tile image to the
     * origin of the object. The object will be rendered such that its
     * origin is at the bottom center of its origin tile. If no origin is
     * specified, the bottom of the image is aligned with the bottom of
     * the origin tile and the left side of the image is aligned with the
     * left edge of the left-most base tile.
     */
    public void setOrigin (int x, int y)
    {
        _originX = x;
        _originY = y;
    }

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", baseWidth=").append(_baseWidth);
        buf.append(", baseHeight=").append(_baseHeight);
        buf.append(", originX=").append(_originX);
        buf.append(", originY=").append(_originY);
    }

    /** The object footprint width in unit tile units. */
    protected int _baseWidth = 1;

    /** The object footprint height in unit tile units. */
    protected int _baseHeight = 1;

    /** The x offset from the origin of the tile image to the object's
     * origin or MIN_VALUE if the origin should be calculated based on the
     * footprint. */
    protected int _originX = Integer.MIN_VALUE;

    /** The y offset from the origin of the tile image to the object's
     * origin or MIN_VALUE if the origin should be calculated based on the
     * footprint. */
    protected int _originY = Integer.MIN_VALUE;
}
