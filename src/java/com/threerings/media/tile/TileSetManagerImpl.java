//
// $Id: TileSetManagerImpl.java,v 1.13 2001/10/11 00:41:26 shaper Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

import com.samskivert.util.*;

import com.threerings.media.ImageManager;
import com.threerings.media.Log;

public abstract class TileSetManagerImpl implements TileSetManager
{
    /**
     * Initialize the <code>TileSetManager</code>.
     *
     * @param config the config object.
     * @param imgmgr the image manager.
     */
    public void init (Config config, ImageManager imgmgr)
    {
	_imgmgr = imgmgr;
        _config = config;
    }

    public int getNumTilesInSet (int tsid)
	throws NoSuchTileSetException
    {
	return getTileSet(tsid).getNumTiles();
    }

    public TileSet getTileSet (int tsid)
	throws NoSuchTileSetException
    {
	TileSet tset = (TileSet)_tilesets.get(tsid);
	if (tset == null) {
	    throw new NoSuchTileSetException(tsid);
	}

	return tset;
    }

    public Tile getTile (int tsid, int tid)
	throws NoSuchTileSetException, NoSuchTileException
    {
	return getTileSet(tsid).getTile(_imgmgr, tid);
    }

    public ArrayList getAllTileSets ()
    {
	ArrayList list = new ArrayList();
	CollectionUtil.addAll(list, _tilesets.elements());
	return list;
    }

    public int getNumTileSets ()
    {
	return _tilesets.size();
    }

    /** The config object. */
    protected Config _config;

    /** The image manager. */
    protected ImageManager _imgmgr;

    /** The available tilesets keyed by tileset id. */
    protected HashIntMap _tilesets = new HashIntMap();
}
