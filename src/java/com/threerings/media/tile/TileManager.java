//
// $Id: TileManager.java,v 1.10 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.miso.Log;

import com.samskivert.util.IntMap;

import java.awt.*;
import java.awt.image.BufferedImage;
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
	int utid = (tsid << 16) | tid;

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

    /** Cache of tiles that have been requested thus far. */
    protected IntMap _tiles = new IntMap();

    /** Our tileset manager. */
    protected TileSetManager _tsmgr;
}
