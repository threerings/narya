//
// $Id: TileSetManagerImpl.java,v 1.14 2001/10/12 00:38:15 shaper Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.io.*;
import java.util.Collections;
import java.util.Iterator;

import com.samskivert.util.*;

import com.threerings.media.ImageManager;
import com.threerings.media.Log;

public abstract class TileSetManagerImpl implements TileSetManager
{
    /**
     * Initialize the tile set manager.
     *
     * @param config the config object.
     * @param imgmgr the image manager.
     */
    public void init (Config config, ImageManager imgmgr)
    {
	_imgmgr = imgmgr;
        _config = config;
    }

    // documentation inherited
    public TileSet getTileSet (int tsid)
	throws NoSuchTileSetException
    {
	TileSet tset = (TileSet)_tilesets.get(tsid);
	if (tset == null) {
	    throw new NoSuchTileSetException(tsid);
	}

	return tset;
    }

    // documentation inherited
    public Iterator getTileSets ()
    {
        return Collections.unmodifiableMap(_tilesets).values().iterator();
    }

    // documentation inherited
    public Tile getTile (int tsid, int tid)
	throws NoSuchTileSetException, NoSuchTileException
    {
	return getTileSet(tsid).getTile(_imgmgr, tid);
    }

    /** The config object. */
    protected Config _config;

    /** The image manager. */
    protected ImageManager _imgmgr;

    /** The available tilesets keyed by tileset id. */
    protected HashIntMap _tilesets = new HashIntMap();
}
