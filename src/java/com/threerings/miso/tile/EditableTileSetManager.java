//
// $Id: EditableTileSetManager.java,v 1.7 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.tile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.samskivert.util.Config;
import com.samskivert.util.ConfigUtil;
import com.threerings.miso.Log;
import com.threerings.media.ImageManager;

/**
 * Extends general tileset manager functionality to allow reading
 * tileset information from XML files.  The XML file format allows for
 * improved version-control, and makes tilesets easier to view, edit,
 * and re-use than might otherwise be the case.
 */
public class EditableTileSetManager extends TileSetManagerImpl
{
    public void init (Config config, ImageManager imgmgr)
    {
        super.init(config, imgmgr);

        // load the tileset descriptions
        String fname = config.getValue("miso.tilesets", (String)null);
        loadTileSets(fname);
    }

    /**
     * Load the tilesets described in the specified file into the set
     * of available tilesets.
     */
    protected void loadTileSets (String fname)
    {
	try {
	    InputStream tis = ConfigUtil.getStream(fname);
	    if (tis == null) {
		Log.warning("Couldn't find file [fname=" + fname + "].");
		return;
	    }

            // read all tileset descriptions from the XML input stream
            XMLTileSetParser parser = new XMLTileSetParser();
            ArrayList tsets = parser.loadTileSets(tis);
            if (tsets == null) {
                Log.warning("No tilesets found [tis=" + tis + "].");
                return;
            }

            // copy new tilesets into the main tileset hashtable
            int size = tsets.size();
            for (int ii = 0; ii < size; ii++) {
                TileSet tset = (TileSet)tsets.get(ii);
                _tilesets.put(tset.getId(), tset);
                Log.info("Adding tileset to cache [tset=" + tset + "].");
            }

        } catch (IOException ioe) {
	    Log.warning("Exception loading tileset [fname=" + fname +
			", ioe=" + ioe + "].");
	}
    }
}
