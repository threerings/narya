//
// $Id: TileManager.java,v 1.4 2001/07/16 22:12:01 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.threerings.cocktail.miso.Log;

import com.samskivert.util.IntMap;

import java.awt.*;

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
     * Return a String array of all tileset names ordered by ascending
     * tileset id.
     */
    public String[] getTileSetNames ()
    {
	return _tsmgr.getTileSetNames();
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

	// retrieve the tile image from the tileset
	tile = new Tile(tsid, tid);
	tile.img = tset.getTileImage(tid);

	_tiles.put(utid, tile);

//  	Log.info("Loaded tile into cache [tsid="+tsid+", tid="+tid+"].");

	return tile;
    }

    // mapping from (tsid << 16 | tid) to tile objects
    protected IntMap _tiles = new IntMap();

    // our tile set manager
    protected TileSetManager _tsmgr;
}
