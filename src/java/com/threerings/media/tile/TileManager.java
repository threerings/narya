//
// $Id: TileManager.java,v 1.13 2001/08/13 15:00:24 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.miso.Log;

import com.samskivert.util.IntMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * The <code>TileManager</code> class provides a simplified interface
 * for retrieving and caching tiles.
 *
 * @see TileSetManager
 */
public class TileManager
{
    /**
     * Initialize the tile manager with the given
     * <code>TileSetManager</code> object.
     *
     * @param tilesetmgr the tileset manager.
     */
    public TileManager (TileSetManager tilesetmgr)
    {
	_tilesetmgr = tilesetmgr;
    }

    /**
     * Return the <code>Tile</code> object for the specified tileset
     * and tile id.
     *
     * @param tsid the tileset id.
     * @param tid the tile id.
     *
     * @return the tile object.
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
	if ((tile.img = _tilesetmgr.getTileImage(tsid, tid)) == null) {
	    Log.warning("Null tile image [tsid="+tsid+", tid="+tid+"].");
	}
        BufferedImage bimg = (BufferedImage)tile.img;
	tile.height = (short)bimg.getHeight();
        tile.width = (short)bimg.getWidth();

	_tiles.put(utid, tile);

//  	Log.info("Loaded tile into cache [tsid="+tsid+", tid="+tid+"].");

	return tile;
    }

    /**
     * Return the TileSetManager that the TileManager is using.
     */
    public TileSetManager getTileSetManager ()
    {
	return _tilesetmgr;
    }

    /** Cache of tiles that have been requested thus far. */
    protected IntMap _tiles = new IntMap();

    /** The tileset manager. */
    protected TileSetManager _tilesetmgr;
}
