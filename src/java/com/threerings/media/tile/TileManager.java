//
// $Id: TileManager.java,v 1.2 2001/07/14 00:21:24 shaper Exp $

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
    
    public Tile getTile (String setName, String tileName)
    {
	return new Tile(0, 0);
    }

    public Tile getTile (short tsid, short tid)
    {
	// the fully unique tile id is the conjoined tile set and tile id
	int utid = tsid << 16 | tid;

	// look the tile up in our hash
	Tile tile = (Tile) _tiles.get(utid);
	if (tile == null) {

	    // retrieve the tileset containing the tile
	    TileSet tset = _tsmgr.getTileSet(tsid);
	    Log.info("Retrieved tileset [tsid=" + tsid + ", tid=" +
		     tid + ", tset=" + tset + "].");

	    // retrieve the tile image from the tileset
	    tile = new Tile(tsid, tid);
	    tile.img = tset.getTileImage(tid);

	    _tiles.put(utid, tile);
	}

	return tile;
    }

    // mapping from (tsid << 16 | tid) to tile objects
    protected IntMap _tiles = new IntMap();

    // mapping from tileset ids to tileset objects
    protected IntMap _tilesets = new IntMap();

    // our tile set manager
    protected TileSetManager _tsmgr;
}
