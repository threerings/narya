//
// $Id: TileSetManagerImpl.java,v 1.5 2001/07/20 02:23:34 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.ImageManager;

import com.samskivert.util.IntMap;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Enumeration;

public abstract class TileSetManagerImpl implements TileSetManager
{
    public TileSetManagerImpl (ImageManager imgr)
    {
	_imgr = imgr;
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
	return (tset != null) ? tset.getTileImage(_imgr, tid) : null;
    }

    public ArrayList getAllTileSets ()
    {
	int size = _tilesets.size();
	if (size == 0) return null;

	ArrayList list = new ArrayList();

	Enumeration sets = _tilesets.elements();
	for (int ii = 0; ii < size; ii++) {
	    list.add(sets.nextElement());
	}

	return list;
    }

    public int getNumTileSets ()
    {
	return _tilesets.size();
    }

    protected ImageManager _imgr;
    protected IntMap _tilesets = new IntMap();
}
