//
// $Id: TileSet.java,v 1.16 2001/10/08 21:04:25 shaper Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.*;

import com.samskivert.util.StringUtil;
import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * A tileset stores information on a single logical set of tiles.  It
 * provides a clean interface for the {@link TileSetManager} to
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
    public TileSet ()
    {
	_model = new TileSetModel();
    }

    /**
     * Return the tileset identifier.
     */
    public int getId ()
    {
	return _model.tsid;
    }

    /**
     * Return the tileset name.
     */
    public String getName ()
    {
	return _model.name;
    }

    /**
     * Return the number of tiles in the tileset.
     */
    public int getNumTiles ()
    {
	return _model.numTiles;
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
	    if ((_imgTiles = imgmgr.getImage(_model.imgFile)) == null) {
		Log.warning("Failed to retrieve full tileset image " +
			    "[file=" + _model.imgFile + "].");
		return null;
	    }
	}

	// find the row number containing the sought-after tile
	int ridx, tcount, ty, tx;
	ridx = tcount = 0;

        // start tile image position at image start offset
        tx = _model.offsetPos.x;
        ty = _model.offsetPos.y;

	while ((tcount += _model.tileCount[ridx]) < tid + 1) {
            // increment tile image position by row height and gap distance
	    ty += (_model.rowHeight[ridx++] + _model.gapDist.y);
	}

        // determine the horizontal index of this tile in the row
	int xidx = tid - (tcount - _model.tileCount[ridx]);

        // final image x-position is based on tile width and gap distance
        tx += (xidx * (_model.rowWidth[ridx] + _model.gapDist.x));

//          Log.info("Retrieving tile image [tid=" + tid + ", ridx=" +
//                   ridx + ", xidx=" + xidx + ", tx=" + tx +
//                   ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
	return imgmgr.getImageCropped(
            _imgTiles, tx, ty, _model.rowWidth[ridx], _model.rowHeight[ridx]);
    }

    /**
     * Return the {@link Tile} object from this tileset corresponding
     * to the specified tile id, or <code>null</code> if no such tile
     * id exists.  The tile image is retrieved from the given image
     * manager.
     *
     * @param imgmgr the image manager.
     * @param tid the tile identifier.
     *
     * @return the tile object, or null if no such tile exists.
     */
    public Tile getTile (ImageManager imgmgr, int tid)
    {
	// bail if there's no such tile
	if (tid > (_model.numTiles - 1)) {
	    return null;
	}

	// create the tile object
	Tile tile = createTile(tid);

	// retrieve the tile image
	tile.img = getTileImage(imgmgr, tid);
	if (tile.img == null) {
	    Log.warning("Null tile image " +
			"[tsid=" + _model.tsid + ", tid=" + tid + "].");
	}

	// populate the tile's dimensions
        BufferedImage bimg = (BufferedImage)tile.img;
	tile.height = (short)bimg.getHeight();
        tile.width = (short)bimg.getWidth();

	return tile;
    }

    /**
     * Returns the tile set model.
     */
    public TileSetModel getModel ()
    {
	return _model;
    }

    /**
     * Return a string representation of the tileset information.
     */
    public String toString ()
    {
	return _model.toString();
    }

    /**
     * Construct and return a new tile object for further population
     * with tile-specific information.
     */
    protected Tile createTile (int tid)
    {
	return new Tile(_model.tsid, tid);
    }

    /** The image containing all tile images for this set. */
    protected Image _imgTiles;

    /** The tile set data model. */
    protected TileSetModel _model;

    /**
     * The model that details the attributes of each tile in a
     * tileset.  Storing the data separately from the tile set object
     * itself allows for the wealth of information associated with a
     * tile set to be more cleanly gathered and passed on to the tile
     * set constructor by those that need to do so.
     */
    public static class TileSetModel
    {
	/** The tileset name. */
	public String name;

	/** The tileset unique identifier. */
	public int tsid;

	/** The file containing the tile images. */
	public String imgFile;

	/** The width of the tiles in each row in pixels. */
	public int[] rowWidth;

	/** The height of the tiles in each row in pixels. */
	public int[] rowHeight;

	/** The number of tiles in each row. */
	public int[] tileCount;

	/** The number of tiles in the tileset. */
	public int numTiles;

	/**
	 * The offset distance (x, y) in pixels from the top-left of the
	 * image to the start of the first tile image.
	 */
	public Point offsetPos = new Point();

	/**
	 * The distance (x, y) in pixels between each tile in each row
	 * horizontally, and between each row of tiles vertically.
	 */
	public Point gapDist = new Point();

	public TileSetModel ()
	{
	}

	public String toString ()
	{
	    StringBuffer buf = new StringBuffer();
	    buf.append("[name=").append(name);
	    buf.append(", file=").append(imgFile);
	    buf.append(", tsid=").append(tsid);
	    buf.append(", numtiles=").append(numTiles);
	    return buf.append("]").toString();
	}
    }
}
