//
// $Id: TileSetManager.java,v 1.5 2001/07/18 22:45:35 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Image;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

public interface TileSetManager
{
    /**
     * Return the tileset object corresponding to the specified tileset id.
     */
    public TileSet getTileSet (int tsid);

    /**
     * Return the image corresponding to the specified tileset and tile id.
     */
    public Image getTileImage (int tsid, int tid);

    /**
     * Return a list of all tilesets available for use.
     */
    public ArrayList getTileSets ();

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
