//
// $Id: UniformTileSet.java,v 1.6 2002/01/17 23:43:14 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;

import com.threerings.media.Log;
import com.threerings.media.ImageUtil;

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
        return _count;
    }

    /**
     * Specifies the number of tiles that will be found in the tileset
     * image managed by this tileset.
     */
    public void setTileCount (int tileCount)
    {
        _count = tileCount;
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
    protected Image extractTileImage (int tileId)
    {
        Image tsimg = getTilesetImage();
        if (tsimg == null) {
            // FIXME: we should really be returning a blank image of the
            // appropriate width and height here rather than null
            return null;
        }

        // figure out from whence to crop the tile
        int tilesPerRow = tsimg.getWidth(null) / _width;
        int row = tileId / tilesPerRow;
        int col = tileId % tilesPerRow;

	// crop the tile-sized image chunk from the full image
	return ImageUtil.getSubimage(
            tsimg, _width*col, _height*row, _width, _height);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", width=").append(_width);
	buf.append(", height=").append(_height);
    }

    /** The total number of tiles in this tileset. */
    protected int _count;

    /** The width (in pixels) of the tiles in this tileset. */
    protected int _width;

    /** The height (in pixels) of the tiles in this tileset. */
    protected int _height;
}
