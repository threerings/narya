//
// $Id: TileSetImpl.java,v 1.2 2001/10/12 16:36:58 shaper Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.*;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

// documentation inherited
public class TileSetImpl implements TileSet
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

    /** Whether this set produces object tiles. */
    public boolean isObjectSet = false;

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

    // documentation inherited
    public int getId ()
    {
	return tsid;
    }

    // documentation inherited
    public String getName ()
    {
	return name;
    }

    // documentation inherited
    public int getNumTiles ()
    {
	return numTiles;
    }

    // documentation inherited
    public Tile getTile (ImageManager imgmgr, int tid)
	throws NoSuchTileException
    {
	// bail if there's no such tile
	if (tid < 0 || tid > (numTiles - 1)) {
	    throw new NoSuchTileException(tid);
	}

	// create and populate the tile object
	Tile tile = createTile(tid);

	// retrieve the tile image
	tile.img = getTileImage(imgmgr, tile.tid);
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
     * Return a string representation of the tileset information.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[name=").append(name);
	buf.append(", file=").append(imgFile);
	buf.append(", tsid=").append(tsid);
	buf.append(", numtiles=").append(numTiles);
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
	if (isObjectSet) {
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

	    return new ObjectTile(tsid, tid, wid, hei);
	}

        // construct a basic tile
	return new Tile(tsid, tid);
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
	    if ((_imgTiles = imgmgr.getImage(imgFile)) == null) {
		Log.warning("Failed to retrieve full tileset image " +
			    "[file=" + imgFile + "].");
		return null;
	    }
	}

	// find the row number containing the sought-after tile
	int ridx, tcount, ty, tx;
	ridx = tcount = 0;

        // start tile image position at image start offset
        tx = offsetPos.x;
        ty = offsetPos.y;

	while ((tcount += tileCount[ridx]) < tid + 1) {
            // increment tile image position by row height and gap distance
	    ty += (rowHeight[ridx++] + gapDist.y);
	}

        // determine the horizontal index of this tile in the row
	int xidx = tid - (tcount - tileCount[ridx]);

        // final image x-position is based on tile width and gap distance
        tx += (xidx * (rowWidth[ridx] + gapDist.x));

	// Log.info("Retrieving tile image [tid=" + tid + ", ridx=" +
	// ridx + ", xidx=" + xidx + ", tx=" + tx +
	// ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
	return imgmgr.getImageCropped(
            _imgTiles, tx, ty, rowWidth[ridx], rowHeight[ridx]);
    }

    protected void addObjectInfo (int tid, int size[])
    {
        if (_objects == null) {
            _objects = new HashIntMap();
        }

        _objects.put(tid, size);
    }

    /** Mapping of object tile ids to object dimensions. */
    protected HashIntMap _objects;

    /** The image containing all tile images for this set. */
    protected Image _imgTiles;
}
