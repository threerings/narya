//
// $Id: SwissArmyTileSet.java,v 1.1 2001/11/08 03:04:44 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Point;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * The swiss army tileset supports a diverse variety of tiles in the
 * tileset image. Each row can contain varying numbers of tiles and each
 * row can have its own width and height. Tiles can be separated from the
 * edge of the tileset image by some border offset and can be separated
 * from one another by a gap distance.
 */
public class SwissArmyTileSet extends TileSet
{
    /**
     * The full monty. Constructs a swiss army tile set object with the
     * full panoply of parameters, specifying everything under the sun.
     * Each row in the tileset image must contain tiles of the same width
     * and height, but those values can vary from row to row. Each row can
     * contain an arbitrary number of tiles. The tiles can be offset from
     * the upper left of the image and can have a uniform horizontal and
     * vertical distance between each tile (horizontal doesn't have to be
     * the same as vertical but all horizontal distances must be the same,
     * for example).
     *
     * @param imgmgr an image manager from which the tileset image can be
     * loaded.
     * @param imgPath the path to the tileset image.
     * @param name the name of the tileset (optional, can be null).
     * @param tsid the unique integer identifier of the tileset (optional,
     * can be zero if the tileset is not to be loaded by id).
     * @param tileCount an array containing the number of tiles in each
     * row.
     * @param rowWidth an array containing the width of the tiles in each
     * row.
     * @param rowHeight an array containing the height of the tiles in
     * each row.
     * @param offsetPos the offset to the upper left of the first tile.
     * @param gapDist the number of pixels (x for horizontally and y for
     * vertically) in between each tile in the tileset image.
     */
    public SwissArmyTileSet (
        ImageManager imgmgr, String imgPath, String name, int tsid,
        int tileCount[], int rowWidth[], int rowHeight[],
        Point offsetPos, Point gapDist)
    {
        super(imgmgr, imgPath, name, tsid);

        // keep these around
        _tileCount = tileCount;
        _rowWidth = rowWidth;
        _rowHeight = rowHeight;
        _offsetPos = offsetPos;
        _gapDist = gapDist;

        // compute our number of tiles
        for (int i = 0; i < tileCount.length; i++) {
            _numTiles += tileCount[i];
        }
    }

    // documentation inherited
    public int getTileCount ()
    {
	return _numTiles;
    }

    // documentation inherited
    protected Image getTileImage (int tileId)
    {
        Image tsimg = getTilesetImage();
        if (tsimg == null) {
            return null;
        }

	// find the row number containing the sought-after tile
	int ridx, tcount, ty, tx;
	ridx = tcount = 0;

        // start tile image position at image start offset
        tx = _offsetPos.x;
        ty = _offsetPos.y;

	while ((tcount += _tileCount[ridx]) < tileId + 1) {
            // increment tile image position by row height and gap distance
	    ty += (_rowHeight[ridx++] + _gapDist.y);
	}

        // determine the horizontal index of this tile in the row
	int xidx = tileId - (tcount - _tileCount[ridx]);

        // final image x-position is based on tile width and gap distance
        tx += (xidx * (_rowWidth[ridx] + _gapDist.x));

	// Log.info("Retrieving tile image [tileId=" + tileId + ", ridx=" +
	// ridx + ", xidx=" + xidx + ", tx=" + tx +
	// ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
	return _imgmgr.getImageCropped(
            tsimg, tx, ty, _rowWidth[ridx], _rowHeight[ridx]);
    }

    /** The number of tiles in each row. */
    protected int[] _tileCount;

    /** The number of tiles in the tileset. */
    protected int _numTiles;

    /** The width of the tiles in each row in pixels. */
    protected int[] _rowWidth;

    /** The height of the tiles in each row in pixels. */
    protected int[] _rowHeight;

    /** The offset distance (x, y) in pixels from the top-left of the
     * image to the start of the first tile image.  */
    protected Point _offsetPos = new Point();

    /** The distance (x, y) in pixels between each tile in each row
     * horizontally, and between each row of tiles vertically.  */
    protected Point _gapDist = new Point();
}
