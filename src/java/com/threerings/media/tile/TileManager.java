//
// $Id: TileManager.java,v 1.17 2001/09/28 00:44:31 shaper Exp $

package com.threerings.media.tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.samskivert.util.HashIntMap;

import com.threerings.media.Log;

/**
 * The tile manager provides a simplified interface for retrieving and
 * caching tiles.
 *
 * @see TileSetManager
 */
public class TileManager
{
    /**
     * Initialize the tile manager.
     *
     * @param tilesetmgr the tileset manager.
     */
    public TileManager (TileSetManager tilesetmgr)
    {
	_tilesetmgr = tilesetmgr;
    }

    /**
     * Returns the {@link Tile} object for the specified tileset and
     * tile id, or null if an error occurred.
     *
     * @param tsid the tileset id.
     * @param tid the tile id.
     *
     * @return the tile object, or null if an error occurred.
     */
    public Tile getTile (int tsid, int tid)
    {
	// the fully unique tile id is the conjoined tile set and tile id
	int utid = (tsid << 16) | tid;

	// look the tile up in our hash
	Tile tile = (Tile) _tiles.get(utid);
	if (tile != null) {
  	    // Log.info("Retrieved tile from cache [tsid=" + tsid +
	    // ", tid=" + tid + "].");
	    return tile;
	}

	// retrieve the tile from the tileset
	tile = _tilesetmgr.getTile(tsid, tid);
	if (tile != null) {
	    // Log.info("Loaded tile into cache [tsid=" + tsid +
	    // ", tid=" + tid + "].");
	    _tiles.put(utid, tile);
	}

	return tile;
    }

    /**
     * Returns the tile set manager used by this tile manager.
     */
    public TileSetManager getTileSetManager ()
    {
	return _tilesetmgr;
    }

    /** Cache of tiles that have been requested thus far. */
    protected HashIntMap _tiles = new HashIntMap();

    /** The tileset manager. */
    protected TileSetManager _tilesetmgr;
}
