//
// $Id: TileSetManager.java,v 1.14 2001/10/12 00:38:15 shaper Exp $

package com.threerings.media.tile;

import com.threerings.media.ImageManager;

import java.awt.Image;
import java.util.Iterator;

/**
 * The <code>TileSetManager</code> provides tileset management
 * functionality intended for use by the <code>TileManager</code>.  It
 * provides facilities for obtaining information about individual
 * tilesets, retrieving an list of all tilesets available, and
 * retrieving the image associated with a particular tile in a set.
 *
 * @see TileManager
 */
public interface TileSetManager
{
    /**
     * Return an <code>Iterator</code> over all <code>TileSet</code>
     * objects available.
     *
     * @return the tileset iterator.
     */
    public Iterator getTileSets ();

    /**
     * Return the tileset object corresponding to the specified
     * tileset id, or null if the tileset is not found.
     *
     * @param tsid the tileset identifier.
     * @return the tileset object.
     */
    public TileSet getTileSet (int tsid)
	throws NoSuchTileSetException;

    /**
     * Return the tile object corresponding to the specified tileset
     * and tile id.
     *
     * @param tsid the tileset identifier.
     * @param tid the tile identifier.
     *
     * @return the tile object.
     */
    public Tile getTile (int tsid, int tid)
	throws NoSuchTileSetException, NoSuchTileException;
}
