//
// $Id: EditableTileSetManager.java,v 1.12 2001/10/12 16:38:59 shaper Exp $

package com.threerings.miso.tile;

import java.io.IOException;
import java.util.List;

import com.samskivert.util.Config;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoUtil;

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

        // load the tilesets from the XML description file
        String fname = config.getValue(TILESETS_KEY, (String)null);
        List tilesets = null;
        try {
            tilesets = new XMLMisoTileSetParser().loadTileSets(fname);
        } catch (IOException ioe) {
	    Log.warning("Exception loading tileset [fname=" + fname +
			", ioe=" + ioe + "].");
            return;
        }

        // bail if we didn't find any tilesets
        int size = tilesets.size();
        if (size == 0) {
            Log.warning("No tilesets found [fname=" + fname + "].");
            return;
        }

        // copy new tilesets into the main tileset hashtable
        for (int ii = 0; ii < size; ii++) {
            TileSet tset = (TileSet)tilesets.get(ii);
            _tilesets.put(tset.getId(), tset);
            Log.info("Adding tileset to cache [tset=" + tset + "].");
        }
    }

    /** The config key for the tileset description file. */
    protected static final String TILESETS_KEY =
	MisoUtil.CONFIG_KEY + ".tilesets";
}
