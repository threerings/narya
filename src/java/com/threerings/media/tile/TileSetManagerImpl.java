//
// $Id: TileSetManagerImpl.java,v 1.7 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Image;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

import com.samskivert.util.*;
import com.threerings.media.ImageManager;

public abstract class TileSetManagerImpl implements TileSetManager
{
    /**
     * Initialize the TileSetManager with a Config object to obtain
     * configuration information and an ImageManager object for use in
     * retrieving tile images.
     */
    public void init (Config config, ImageManager imgmgr)
    {
	_imgmgr = imgmgr;
        _config = config;
    }

    public int getNumTilesInSet (int tsid)
    {
	TileSet tset = getTileSet(tsid);
	return (tset != null) ? tset.getNumTiles() : -1;
    }

    public TileSet getTileSet (int tsid)
    {
	return (TileSet)_tilesets.get(tsid);
    }

    public Image getTileImage (int tsid, int tid)
    {
	TileSet tset = getTileSet(tsid);
	return (tset != null) ? tset.getTileImage(_imgmgr, tid) : null;
    }

    public ArrayList getAllTileSets ()
    {
	int size = _tilesets.size();
	if (size == 0) return null;

	ArrayList list = new ArrayList();
	CollectionUtil.addAll(list, _tilesets.elements());
	return list;
    }

    public int getNumTileSets ()
    {
	return _tilesets.size();
    }

    protected Config _config;
    protected ImageManager _imgmgr;
    protected IntMap _tilesets = new IntMap();
}
