//
// $Id: TileSetManagerImpl.java,v 1.1 2001/07/16 18:59:31 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.samskivert.util.Config;
import com.samskivert.util.IntMap;

import java.util.Enumeration;

public class TileSetManagerImpl implements TileSetManager
{
    public TileSetManagerImpl (Config config)
    {
	_config = config;
    }

    public TileSet getTileSet (int tsid)
    {
	TileSet tset = (TileSet)_tilesets.get(tsid);
	if (tset != null) return tset;

	_tilesets.put(tsid, tset = new TileSet(_config, tsid));
	return tset;
    }

    public String[] getTileSetNames ()
    {
	int size = _tilesets.size();
	if (size == 0) return null;

	String[] names = new String[size];
	Enumeration sets = _tilesets.elements();
	for (int ii = 0; ii < size; ii++) {
	    names[ii] = ((TileSet)sets.nextElement()).getName();
	}

	return names;
    }

    public int getTileSetId (String name)
    {
	Enumeration sets = _tilesets.elements();
	while (sets.hasMoreElements()) {
	    TileSet tset = (TileSet)sets.nextElement();
	    if (tset.getName().equals(name)) return tset.getId();
	}

	return -1;
    }

    public int getNumTileSets ()
    {
	return _tilesets.size();
    }

    protected Config _config;
    protected IntMap _tilesets = new IntMap();
}
