//
// $Id: TileManager.java,v 1.19 2001/11/01 01:40:42 shaper Exp $

package com.threerings.media.tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.samskivert.util.HashIntMap;

import com.threerings.media.Log;

/**
 * The tile manager provides a simplified interface for retrieving and
 * caching tiles.
 */
public class TileManager
{
    /**
     * Initializes the tile manager.
     *
     * @param tilesetrepo the tile set repository.
     */
    public TileManager (TileSetRepository tsrepo)
    {
	_tsrepo = tsrepo;
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
	throws NoSuchTileSetException, NoSuchTileException
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
	tile = _tsrepo.getTileSet(tsid).getTile(tid);
	if (tile != null) {
	    // Log.info("Loaded tile into cache [tsid=" + tsid +
	    // ", tid=" + tid + "].");
	    _tiles.put(utid, tile);
	}

	return tile;
    }

    /**
     * Returns the tile set repository used by this tile manager.
     */
    public TileSetRepository getTileSetRepository ()
    {
	return _tsrepo;
    }

    /** Cache of tiles that have been requested thus far. */
    protected HashIntMap _tiles = new HashIntMap();

    /** The tile set repository. */
    protected TileSetRepository _tsrepo;
}
