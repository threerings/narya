//
// $Id: TileSetManager.java,v 1.7 2001/07/21 01:51:10 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.ImageManager;

import java.awt.Image;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

public interface TileSetManager
{
    /**
     * Initialize the TileSetManager with the given ImageManager.
     */
    public void init (ImageManager imgmgr);

    /**
     * Return the total number of tiles in the specified tileset.
     */
    public int getNumTilesInSet (int tsid);

    /**
     * Return a list of all tilesets available for use.
     */
    public ArrayList getAllTileSets ();

    /**
     * Return the tileset object corresponding to the specified tileset id.
     */
    public TileSet getTileSet (int tsid);

    /**
     * Return the image corresponding to the specified tileset and tile id.
     */
    public Image getTileImage (int tsid, int tid);

    /**
     * Return the total number of tilesets.
     */
    public int getNumTileSets ();

    /**
     * Load the tilesets described in the specified input stream into
     * the set of available tilesets.
     */
    public void loadTileSets (InputStream tis) throws IOException;
}
