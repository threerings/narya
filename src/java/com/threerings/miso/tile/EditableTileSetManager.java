//
// $Id: EditableTileSetManager.java,v 1.1 2001/07/14 00:02:47 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.samskivert.util.Config;
import com.samskivert.util.IntMap;

public class EditableTileSetManager implements TileSetManager
{
    public EditableTileSetManager (Config config)
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

    protected Config _config;
    protected IntMap _tilesets = new IntMap();
}
