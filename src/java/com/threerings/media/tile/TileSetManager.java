//
// $Id: TileSetManager.java,v 1.9 2001/07/23 22:31:48 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.ImageManager;

import java.awt.Image;
import java.util.ArrayList;

/**
 * The TileSetManager provides tileset management functionality
 * intended for use by the TileManager.  It provides facilities for
 * obtaining information about individual tilesets, retrieving an list
 * of all tilesets available, and retrieving the image associated with
 * a particular tile in a set.
 */
public interface TileSetManager
{
    /**
     * Return the total number of tiles in the specified tileset or -1
     * if the tileset is not found.
     *
     * @param tsid the tileset identifier.
     * @return the number of tiles.
     */
    public int getNumTilesInSet (int tsid);

    /**
     * Return an ArrayList containing all TileSet objects available.
     *
     * @return the list of tilesets.
     */
    public ArrayList getAllTileSets ();

    /**
     * Return the tileset object corresponding to the specified
     * tileset id, or null if the tileset is not found.
     *
     * @param tsid the tileset identifier.
     * @return the tileset object.
     */
    public TileSet getTileSet (int tsid);

    /**
     * Return the image corresponding to the specified tileset and tile id.
     *
     * @param tsid the tileset identifier.
     * @param tid the tile identifier.
     *
     * @return the tile image.
     */
    public Image getTileImage (int tsid, int tid);

    /**
     * Return the total number of tilesets available for use.
     *
     * @return the number of tilesets.
     */
    public int getNumTileSets ();
}
