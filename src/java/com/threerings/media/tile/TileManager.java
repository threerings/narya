//
// $Id: TileManager.java,v 1.9 2001/07/21 01:51:10 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.miso.Log;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.IntMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * Provides a simplified interface for managing multiple tilesets and
 * tiles.
 */
public class TileManager
{
    /**
     * Initialize the tile manager with the given TileSetManager object.
     */
    public TileManager (TileSetManager tsmgr)
    {
	_tsmgr = tsmgr;
    }

    /**
     * Return the Tile object for the specified tileset and tile id.
     */
    public Tile getTile (int tsid, int tid)
    {
	// the fully unique tile id is the conjoined tile set and tile id
	int utid = tsid << 16 | tid;

	// look the tile up in our hash
	Tile tile = (Tile) _tiles.get(utid);
	if (tile != null) {
//  	    Log.info("Retrieved tile from cache [tsid=" + tsid +
//  		     ", tid=" + tid + "].");
	    return tile;
	}

	// retrieve the tile image from the tileset
	tile = new Tile(tsid, tid);
	if ((tile.img = _tsmgr.getTileImage(tsid, tid)) == null) {
	    Log.warning("Null tile image [tsid="+tsid+", tid="+tid+"].");
	}
	tile.height = (short)((BufferedImage)tile.img).getHeight();

	_tiles.put(utid, tile);

//  	Log.info("Loaded tile into cache [tsid="+tsid+", tid="+tid+"].");

	return tile;
    }

    /**
     * Return the TileSetManager that the TileManager is using.
     */
    public TileSetManager getTileSetManager ()
    {
	return _tsmgr;
    }

    /**
     * Load all tileset objects described in the specified file into
     * the set of available tilesets.
     */
    public void loadTileSets (String fname)
    {
	try {
	    InputStream tis = ConfigUtil.getStream(fname);
	    if (tis == null) {
		Log.warning("Couldn't find file [fname=" + fname + "].");
		return;
	    }

	    _tsmgr.loadTileSets(tis);

	} catch (IOException ioe) {
	    Log.warning("Exception loading tileset [fname=" + fname +
			", ioe=" + ioe + "].");
	}
    }

    // mapping from (tsid << 16 | tid) to tile objects
    protected IntMap _tiles = new IntMap();

    // our tile set manager
    protected TileSetManager _tsmgr;
}
