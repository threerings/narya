//
// $Id: TileManager.java,v 1.3 2001/07/16 18:59:31 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.threerings.cocktail.miso.Log;

import com.samskivert.util.IntMap;

import java.awt.*;

/**
 * Provides a simplified interface for retrieving tile objects from
 * various tilesets, by name or identifier, and manages caching of
 * tiles and related resources as appropriate.
 */
public class TileManager
{
    public TileManager (TileSetManager tsmgr)
    {
	_tsmgr = tsmgr;
    }

    public int getNumTilesInSet (int tsid)
    {
	TileSet tset = _tsmgr.getTileSet(tsid);
	if (tset == null) return -1;
	return tset.getNumTiles();
    }

    public String[] getTileSetNames ()
    {
	return _tsmgr.getTileSetNames();
    }

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

	// retrieve the tile image from the tileset
	tile = new Tile(tsid, tid);
	tile.img = tset.getTileImage(tid);

	_tiles.put(utid, tile);

	Log.info("Loaded tile into cache [tsid="+tsid+", tid="+tid+"].");

	return tile;
    }

    // mapping from (tsid << 16 | tid) to tile objects
    protected IntMap _tiles = new IntMap();

    // our tile set manager
    protected TileSetManager _tsmgr;
}
