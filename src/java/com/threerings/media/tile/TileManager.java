//
// $Id: TileManager.java,v 1.5 2001/07/17 17:21:33 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.threerings.cocktail.miso.Log;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.IntMap;

import java.awt.*;
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
     * Return the total number of tiles in the specified tileset.
     */
    public int getNumTilesInSet (int tsid)
    {
	TileSet tset = _tsmgr.getTileSet(tsid);
	if (tset == null) return -1;
	return tset.getNumTiles();
    }

    /**
     * Return a list of all tilesets available for use.
     */
    public ArrayList getTileSets ()
    {
	return _tsmgr.getTileSets();
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

	// retrieve the tileset containing the tile
	TileSet tset = _tsmgr.getTileSet(tsid);
	if (tset == null) {
	    Log.warning("Can't create tile due to unknown tileset " +
			"[tsid=" + tsid + ", tid=" + tid + "].");
	    return null;
	}

	// retrieve the tile image from the tileset
	tile = new Tile(tsid, tid);
	if ((tile.img = tset.getTileImage(tid)) == null) {
	    Log.warning("Null tile image [tsid="+tsid+", tid="+tid+"].");
	}

	_tiles.put(utid, tile);

//  	Log.info("Loaded tile into cache [tsid="+tsid+", tid="+tid+"].");

	return tile;
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
