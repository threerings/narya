//
// $Id: EditableTileSetManager.java,v 1.6 2001/07/21 01:51:10 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.miso.Log;
import com.threerings.media.ImageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Extends general tileset manager functionality to allow reading
 * tileset information from XML files.  The XML file format allows for
 * improved version-control, and makes tilesets easier to view, edit,
 * and re-use than might otherwise be the case.
 */
public class EditableTileSetManager extends TileSetManagerImpl
{
    public void loadTileSets (InputStream tis) throws IOException
    {
	// read all tileset descriptions from the XML input stream
	XMLTileSetParser parser = new XMLTileSetParser();
	parser.loadTileSets(tis);

	// grab any resulting tileset objects
	ArrayList tsets = parser.getTileSets();
	if (tsets == null) {
	    Log.warning("No tileset descriptions found [tis=" + tis + "].");
	    return;
	}

	// and copy them into the main tileset hashtable
	int size = tsets.size();
	for (int ii = 0; ii < size; ii++) {
	    TileSet tset = (TileSet)tsets.get(ii);
	    _tilesets.put(tset.getId(), tset);
	    Log.info("Adding tileset to cache [tset=" + tset + "].");
	}
    }
}
