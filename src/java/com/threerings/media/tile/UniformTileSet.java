//
// $Id: UniformTileSet.java,v 1.1 2001/11/08 03:04:44 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * A uniform tileset is one that is composed of tiles that are all the
 * same width and height and are arranged into rows, with each row having
 * the same number of tiles except possibly the final row which can
 * contain the same as or less than the number of tiles contained by the
 * previous rows.
 */
public class UniformTileSet extends TileSet
{
    /**
     * Constructs a tile set object with the specified tileset
     * configuration parameters.
     *
     * @param imgmgr the image manager via which to load the tileset
     * image.
     * @param imgPath the path to supply to the image manager when loading
     * the tile (which will fetch the image using the resource manager).
     * @param count the number of tiles in the tile image.
     * @param width the width of each tile, in pixels.
     * @param height the height of each tile, in pixels.
     */
    public UniformTileSet (ImageManager imgmgr, String imgPath,
                           int count, int width, int height)
    {
        super(imgmgr, imgPath, null, 0);

        // keep these for later
        _count = count;
        _width = width;
        _height = height;
    }

    // documentation inherited
    public int getTileCount ()
    {
        return _count;
    }

    // documentation inherited
    protected Image getTileImage (int tileId)
    {
        Image tsimg = getTilesetImage();
        if (tsimg == null) {
            return null;
        }

        // figure out from whence to crop the tile
        int tilesPerRow = tsimg.getWidth(null) / _width;
        int row = tilesPerRow / tileId;
        int col = tilesPerRow % tileId;

	// crop the tile-sized image chunk from the full image
	return _imgmgr.getImageCropped(
            tsimg, col * _width, row * _height, _width, _height);
    }

    /** The total number of tiles in this tileset. */
    protected int _count;

    /** The width (in pixels) of the tiles in this tileset. */
    protected int _width;

    /** The height (in pixels) of the tiles in this tileset. */
    protected int _height;
}
