//
// $Id: TileSetManagerImpl.java,v 1.2 2001/07/17 17:21:33 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.samskivert.util.IntMap;

import java.util.ArrayList;
import java.util.Enumeration;

public abstract class TileSetManagerImpl implements TileSetManager
{
    public TileSet getTileSet (int tsid)
    {
	return (TileSet)_tilesets.get(tsid);
    }

    public ArrayList getTileSets ()
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

    protected IntMap _tilesets = new IntMap();
}
