//
// $Id: XMLTileSetRepository.java,v 1.1 2001/11/01 01:40:42 shaper Exp $

package com.threerings.miso.tile;

import java.io.IOException;
import java.util.*;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoUtil;

/**
 * Extends general tile set repository functionality to read tile set
 * descriptions from an XML file.
 */
public class XMLFileTileSetRepository implements TileSetRepository
{
    // documentation inherited
    public void init (Config config, ImageManager imgmgr)
    {
        // get the tile set description file path
        String fname = config.getValue(TILESETS_KEY, DEFAULT_TILESETS);

        // load the tilesets from the XML description file
        try {
            XMLMisoTileSetParser p = new XMLMisoTileSetParser(imgmgr);
            p.loadTileSets(fname, _tilesets);
        } catch (IOException ioe) {
            Log.warning("Exception loading tile sets [ioe=" + ioe + "].");
            return;
        }
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
    public Iterator enumerateTileSets ()
    {
        return Collections.unmodifiableMap(_tilesets).values().iterator();
    }

    /** The config key for the tileset description file. */
    protected static final String TILESETS_KEY =
	MisoUtil.CONFIG_KEY + ".tilesets";

    /** The default tileset description file. */
    protected static final String DEFAULT_TILESETS =
	"rsrc/config/miso/tilesets.xml";

    /** The available tilesets keyed by tileset id. */
    protected HashIntMap _tilesets = new HashIntMap();
}
