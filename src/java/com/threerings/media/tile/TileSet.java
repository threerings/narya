//
// $Id: TileSet.java,v 1.7 2001/07/18 21:45:42 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.miso.Log;
import com.threerings.media.ImageManager;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.*;

/**
 * A tileset stores information on a single logical set of tiles.  It
 * provides a clean interface for the TileManager to retrieve
 * individual tile images from a particular tile in the tileset.
 *
 * The width of each tile in every tileset is a constant Tile.WIDTH in
 * pixels.  The tile count in each row can vary.  The height of the
 * tiles in each row can also vary.  This information is obtained from
 * the config object.
 *
 * Tiles are retrieved from the tile set by the TileManager, and are
 * referenced by their tile id (essentially the tile number, assuming
 * the tile at the top-left of the image is tile id 0 and tiles are
 * numbered left to right, top to bottom, in ascending order.
 *
 * @see com.threerings.miso.TileManager
 */
public class TileSet
{
    public TileSet (String name, int tsid, String imgFile,
		    int[] rowHeight, int[] tileCount)
    {
	_name = name;
	_tsid = tsid;
	_imgFile = imgFile;
	_rowHeight = rowHeight;
	_tileCount = tileCount;

	// determine the total number of tiles in the set
	for (int ii = 0; ii < _tileCount.length; ii++)
	    _numTiles += _tileCount[ii];
    }

    public int getId ()
    {
	return _tsid;
    }

    public String getName ()
    {
	return _name;
    }

    public int getNumTiles ()
    {
	return _numTiles;
    }

    /**
     * Return the image corresponding to the specified tile id within
     * this tile set.
     */
    public BufferedImage getTileImage (int tid)
    {
	// load the full tile image if we don't already have it
	if (_imgTiles == null) {
	    if ((_imgTiles = ImageManager.getImage(_imgFile)) == null) {
		Log.warning("Failed to retrieve full tileset image " +
			    "[file=" + _imgFile + "].");
		return null;
	    }
	}

	// find the row number containing the sought-after tile
	int ridx, tcount, ty, tx;
	ridx = tcount = ty = tx = 0;
	while ((tcount += _tileCount[ridx]) < tid + 1) {
	    ty += _rowHeight[ridx++];
	}

	// determine the horizontal index of this tile in the row
	int xidx = tid - (tcount - _tileCount[ridx]);
	tx = Tile.WIDTH * xidx;

//    	Log.info("Retrieving tile image [tid=" + tid + ", ridx=" +
//    		 ridx + ", xidx=" + xidx + ", tx=" + tx +
//    		 ", ty=" + ty + "].");

	// crop the tile-sized image chunk from the full image
	return ImageManager.getImageCropped(_imgTiles, tx, ty,
					    Tile.WIDTH, _rowHeight[ridx]);
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

	buf.append(", rowheight={");
	for (int ii = 0; ii < _rowHeight.length; ii++) {
	    if (ii > 0) buf.append(",");
	    buf.append(_rowHeight[ii]);
	}

	buf.append("}, tilecount={");
	for (int ii = 0; ii < _tileCount.length; ii++) {
	    if (ii > 0) buf.append(",");
	    buf.append(_tileCount[ii]);
	}

	return buf.append("}]").toString();
    }

    protected String _name;      // the tileset name
    protected String _imgFile;   // the file containing the tile images
    protected int _tsid;         // the tileset unique identifier
    protected int _rowHeight[];  // the height of each row in pixels
    protected int _tileCount[];  // the number of tiles in each row
    protected int _numTiles;     // the total number of tiles

    protected Image _imgTiles;
}
