//
// $Id: TileManager.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.samskivert.util.IntMap;

import java.awt.*;
import java.awt.image.ImageObserver;

import java.util.Vector;

/**
 * Provides a simplified interface for retrieving tile objects from
 * various tilesets, by name or identifier, and manages caching of
 * tiles and related resources as appropriate.
 */
public class TileManager implements ImageObserver
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

	    // retrieve the tile image from the tileset
	    tile = new Tile(tsid, tid);
	    tile.img = tset.getTileImage(tid, this);
	    _tiles.put(utid, tile);
	}

	return tile;
    }

    public void registerObserver (ImageObserver obs)
    {
	if (_observers == null) _observers = new Vector();
	_observers.addElement(obs);
    }

    public boolean imageUpdate (Image img, int infoflags, int x, int y, 
				int width, int height)
    {
	int size = _observers.size();
	for (int ii = 0; ii < size; ii++) {
	    ImageObserver obs = (ImageObserver)_observers.elementAt(ii);
	    obs.imageUpdate(img, infoflags, x, y, width, height);
	}

	return true;
    }

    // mapping from (tsid << 16 | tid) to tile objects
    protected IntMap _tiles = new IntMap();

    // mapping from tileset ids to tileset objects
    protected IntMap _tilesets = new IntMap();

    // registered tile image observers
    protected Vector _observers;

    // our tile set manager
    protected TileSetManager _tsmgr;
}
