//
// $Id: TileSetManager.java,v 1.8 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.ImageManager;

import java.awt.Image;
import java.util.ArrayList;

public interface TileSetManager
{
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
}
