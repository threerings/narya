//
// $Id: TileSet.java,v 1.18 2001/11/01 01:40:42 shaper Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.*;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * A tile set stores information on a single logical set of tiles.  It
 * provides a clean interface for the {@link TileManager} to retrieve
 * individual tiles from the tile set.
 *
 * <p> Tiles are referenced by their tile id.  The tile id is
 * essentially the tile number, assuming the tile at the top-left of
 * the image is tile id 0 and tiles are numbered left to right, top to
 * bottom, in ascending order.
 */
public class TileSet implements Cloneable
{
    /**
     * Constructs a tile set object with the given image manager as
     * the source for retrieving tile images.
     */
    public TileSet (
        ImageManager imgmgr, int tsid, String name, String imgFile,
        int tileCount[], int rowWidth[], int rowHeight[],
        int numTiles, Point offsetPos, Point gapDist,
        boolean isObjectSet, HashIntMap objects)
    {
        _imgmgr = imgmgr;
        _tsid = tsid;
        _name = name;
        _imgFile = imgFile;
        _tileCount = tileCount;
        _rowWidth = rowWidth;
        _rowHeight = rowHeight;
        _numTiles = numTiles;
        _offsetPos = offsetPos;
        _gapDist = gapDist;
        _isObjectSet = isObjectSet;
        _objects = objects;
    }

    /**
     * Returns a new tile set that is a clone of this tile set with
     * the image file updated to reference the given file name.
     */
    public TileSet clone (String imgFile)
        throws CloneNotSupportedException
    {
        TileSet dup = (TileSet)clone();
        dup.setImageFile(imgFile);
        return dup;
    }

    /**
     * Returns the tile set identifier.
     */
    public int getId ()
    {
	return _tsid;
    }

    /**
     * Returns the tile set name.
     */
    public String getName ()
    {
	return _name;
    }

    /**
     * Returns the number of tiles in the tile set.
     */
    public int getNumTiles ()
    {
	return _numTiles;
    }

    /**
     * Returns the {@link Tile} object from this tile set
     * corresponding to the specified tile id, or null if an error
     * occurred.
     *
     * @param tid the tile identifier.
     *
     * @return the tile object, or null if an error occurred.
     */
    public Tile getTile (int tid)
        throws NoSuchTileException
    {
        if (_imgmgr == null) {
            Log.warning("No default image manager [tsid=" + _tsid +
                        ", tid=" + tid + "].");
            return null;
        }

	// bail if there's no such tile
	if (tid < 0 || tid > (_numTiles - 1)) {
	    throw new NoSuchTileException(tid);
	}

	// create and populate the tile object
	Tile tile = createTile(tid);

	// retrieve the tile image
	tile.img = getTileImage(_imgmgr, tile.tid);
	if (tile.img == null) {
	    Log.warning("Null tile image [tile=" + tile + "].");
	}

	// populate the tile's dimensions
        BufferedImage bimg = (BufferedImage)tile.img;
	tile.height = (short)bimg.getHeight();
        tile.width = (short)bimg.getWidth();

	// allow sub-classes to fill in their tile information
	populateTile(tile);

	return tile;
    }

    /**
     * Sets the image file to be used as the source for the tile
     * images produced by this tile set.
     */
    public void setImageFile (String imgFile)
    {
        _imgFile = imgFile;
        _imgTiles = null;
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

    /**
     * Construct and return a new tile object for further population
     * with tile-specific information.  Derived classes can override
     * this method to create their own sub-class of <code>Tile</code>.
     *
     * @param tid the tile id for the new tile.
     *
     * @return the new tile object.
     */
    protected Tile createTile (int tid)
    {
        // construct an object tile if the tile set was specified as such
	if (_isObjectSet) {
            // default object dimensions to (1, 1)
            int wid = 1, hei = 1;

            // retrieve object dimensions if known
            if (_objects != null) {
                int size[] = (int[])_objects.get(tid);
                if (size != null) {
                    wid = size[0];
                    hei = size[1];
                }
            }

	    return new ObjectTile(_tsid, tid, wid, hei);
	}

        // construct a basic tile
	return new Tile(_tsid, tid);
    }

    /**
     * Populates the given tile object with its detailed tile
     * information.  Derived classes can override this method to add
     * in their own tile information, but should be sure to call
     * <code>super.populateTile()</code>.
     *
     * @param tile the tile to populate.
     */
    protected void populateTile (Tile tile)
    {
	// nothing for now
    }

    /**
     * Returns the image corresponding to the specified tile id within
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

	// Log.info("Retrieving tile image [tid=" + tid + ", ridx=" +
	// ridx + ", xidx=" + xidx + ", tx=" + tx +
	// ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
	return imgmgr.getImageCropped(
            _imgTiles, tx, ty, _rowWidth[ridx], _rowHeight[ridx]);
    }

    /** The tileset name. */
    protected String _name;

    /** The tileset unique identifier. */
    protected int _tsid;

    /** The file containing the tile images. */
    protected String _imgFile;

    /** The width of the tiles in each row in pixels. */
    protected int[] _rowWidth;

    /** The height of the tiles in each row in pixels. */
    protected int[] _rowHeight;

    /** The number of tiles in each row. */
    protected int[] _tileCount;

    /** The number of tiles in the tileset. */
    protected int _numTiles;

    /** Whether this set produces object tiles. */
    protected boolean _isObjectSet = false;

    /** The offset distance (x, y) in pixels from the top-left of the
     * image to the start of the first tile image.  */
    protected Point _offsetPos = new Point();

    /** The distance (x, y) in pixels between each tile in each row
     * horizontally, and between each row of tiles vertically.  */
    protected Point _gapDist = new Point();

    /** Mapping of object tile ids to object dimensions. */
    protected HashIntMap _objects;

    /** The image containing all tile images for this set. */
    protected Image _imgTiles;

    /** The default image manager for retrieving tile images. */
    protected ImageManager _imgmgr;
}
