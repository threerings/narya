//
// $Id: UniformTileSet.java,v 1.14 2003/05/13 21:33:58 ray Exp $

package com.threerings.media.tile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * A uniform tileset is one that is composed of tiles that are all the
 * same width and height and are arranged into rows, with each row having
 * the same number of tiles except possibly the final row which can
 * contain the same as or less than the number of tiles contained by the
 * previous rows.
 */
public class UniformTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
        BufferedImage tsimg = getRawTileSetImage();
        int perRow = tsimg.getWidth() / _width;
        int perCol = tsimg.getHeight() / _height;
        return perRow * perCol;
    }

    /**
     * Specifies the width of the tiles in this tileset.
     */
    public void setWidth (int width)
    {
        _width = width;
    }

    /**
     * Returns the width of the tiles in this tileset.
     */
    public int getWidth ()
    {
        return _width;
    }

    /**
     * Specifies the height of the tiles in this tileset.
     */
    public void setHeight (int height)
    {
        _height = height;
    }

    /**
     * Returns the height of the tiles in this tileset.
     */
    public int getHeight ()
    {
        return _height;
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex)
    {
        BufferedImage tsimg = getRawTileSetImage();

        // figure out from whence to crop the tile
        int tilesPerRow = tsimg.getWidth() / _width;

        // if we got a bogus image, return bogus tile bounds
        if (tilesPerRow == 0) {
            return new Rectangle(0, 0, tsimg.getWidth(), tsimg.getHeight());
        }

        int row = tileIndex / tilesPerRow;
        int col = tileIndex % tilesPerRow;

	// crop the tile-sized image chunk from the full image
        return new Rectangle(_width*col, _height*row, _width, _height);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", width=").append(_width);
	buf.append(", height=").append(_height);
    }

    /** The width (in pixels) of the tiles in this tileset. */
    protected int _width;

    /** The height (in pixels) of the tiles in this tileset. */
    protected int _height;
}
