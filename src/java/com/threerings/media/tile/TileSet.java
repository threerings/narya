//
// $Id: TileSet.java,v 1.13 2001/08/13 19:54:39 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.*;

import com.samskivert.util.StringUtil;
import com.threerings.miso.Log;
import com.threerings.media.ImageManager;

/**
 * A tileset stores information on a single logical set of tiles.  It
 * provides a clean interface for the <code>TileSetManager</code> to
 * retrieve individual tiles from the tileset.
 *
 * <p> Tiles are referenced by their tile id.  The tile id is
 * essentially the tile number, assuming the tile at the top-left of
 * the image is tile id 0 and tiles are numbered left to right, top to
 * bottom, in ascending order.
 */
public class TileSet
{
    /**
     * Construct a new <code>TileSet</code> object.
     */
    public TileSet (
	String name, int tsid, String imgFile,
	int[] rowWidth, int[] rowHeight, int[] tileCount, int[] passable,
	Point offsetPos, Point gapDist)
    {
	_name = name;
	_tsid = tsid;
	_imgFile = imgFile;
        _rowWidth = rowWidth;
	_rowHeight = rowHeight;
	_tileCount = tileCount;
	_passable = passable;
        _offsetPos = offsetPos;
        _gapDist = gapDist;

	// determine the total number of tiles in the set
	for (int ii = 0; ii < _tileCount.length; ii++) {
	    _numTiles += _tileCount[ii];
	}
    }

    /**
     * Return the tileset identifier.
     */
    public int getId ()
    {
	return _tsid;
    }

    /**
     * Return the tileset name.
     */
    public String getName ()
    {
	return _name;
    }

    /**
     * Return the number of tiles in the tileset.
     */
    public int getNumTiles ()
    {
	return _numTiles;
    }

    /**
     * Return the image corresponding to the specified tile id within
     * this tile set.
     *
     * @param imgmgr the image manager.
     * @param tid the tile id.
     *
     * @return the tile image.
     */
    protected Image getTileImage (ImageManager imgmgr, int tid)
    {
	// load the full tile image if we don't already have it
	if (_imgTiles == null) {
	    if ((_imgTiles = imgmgr.getImage(_imgFile)) == null) {
		Log.warning("Failed to retrieve full tileset image " +
			    "[file=" + _imgFile + "].");
		return null;
	    }
	}

	// find the row number containing the sought-after tile
	int ridx, tcount, ty, tx;
	ridx = tcount = 0;

        // start tile image position at image start offset
        tx = _offsetPos.x;
        ty = _offsetPos.y;

	while ((tcount += _tileCount[ridx]) < tid + 1) {
            // increment tile image position by row height and gap distance
	    ty += (_rowHeight[ridx++] + _gapDist.y);
	}

        // determine the horizontal index of this tile in the row
	int xidx = tid - (tcount - _tileCount[ridx]);

        // final image x-position is based on tile width and gap distance
        tx += (xidx * (_rowWidth[ridx] + _gapDist.x));

//          Log.info("Retrieving tile image [tid=" + tid + ", ridx=" +
//                   ridx + ", xidx=" + xidx + ", tx=" + tx +
//                   ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
	return imgmgr.getImageCropped(
            _imgTiles, tx, ty, _rowWidth[ridx], _rowHeight[ridx]);
    }

    /**
     * Return the <code>Tile</code> object from this tileset
     * corresponding to the specified tile id.  The tile image is
     * retrieved from the given image manager.
     *
     * @param imgmgr the image manager.
     * @param tid the tile identifier.
     *
     * @return the tile object.
     */
    public Tile getTile (ImageManager imgmgr, int tid)
    {
	// create the tile object
	Tile tile = new Tile(_tsid, tid);

	// retrieve the tile image
	tile.img = getTileImage(imgmgr, tid);
	if (tile.img == null) {
	    Log.warning("Null tile image " +
			"[tsid=" + _tsid + ", tid=" + tid + "].");
	}

	// populate the tile's dimensions
        BufferedImage bimg = (BufferedImage)tile.img;
	tile.height = (short)bimg.getHeight();
        tile.width = (short)bimg.getWidth();

	// and its passability
	tile.passable = (_passable[tid] == 1);

	return tile;
    }

    /**
     * Return a string representation of the tileset information.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[name=").append(_name);
	buf.append(", file=").append(_imgFile);
	buf.append(", tsid=").append(_tsid);
	buf.append(", numtiles=").append(_numTiles);
	return buf.append("]").toString();
    }

    /** The tileset name. */
    protected String _name;

    /** The file containing the tile images. */
    protected String _imgFile;

    /** The tileset unique identifier. */
    protected int _tsid;

    /** The width of the tiles in each row in pixels. */
    protected int _rowWidth[];

    /** The height of each row in pixels. */
    protected int _rowHeight[];

    /** The number of tiles in each row. */
    protected int _tileCount[];

    /** Whether each tile is passable. */
    protected int _passable[];

    /**
     * The offset distance (x, y) in pixels from the top-left of the
     * image to the start of the first tile image.
     */
    protected Point _offsetPos;

    /**
     * The distance (x, y) in pixels between each tile in each row
     * horizontally, and between each row of tiles vertically.
     */
    protected Point _gapDist;

    /** The total number of tiles. */
    protected int _numTiles;

    /** The image containing all tile images for this set. */
    protected Image _imgTiles;
}
