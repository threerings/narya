//
// $Id: TileSet.java,v 1.17 2001/10/11 00:41:26 shaper Exp $

package com.threerings.media.tile;

import com.threerings.media.ImageManager;

/**
 * A tileset stores information on a single logical set of tiles.  It
 * provides a clean interface for the {@link TileSetManager} to
 * retrieve individual tiles from the tileset.
 *
 * <p> Tiles are referenced by their tile id.  The tile id is
 * essentially the tile number, assuming the tile at the top-left of
 * the image is tile id 0 and tiles are numbered left to right, top to
 * bottom, in ascending order.
 */
public interface TileSet
{
    /**
     * Return the tileset identifier.
     */
    public int getId ();

    /**
     * Return the tileset name.
     */
    public String getName ();

    /**
     * Return the number of tiles in the tileset.
     */
    public int getNumTiles ();

    /**
     * Returns the {@link Tile} object from this tileset corresponding
     * to the specified tile id, or <code>null</code> if no such tile
     * id exists.  The tile image is retrieved from the given image
     * manager.
     *
     * @param imgmgr the image manager.
     * @param tid the tile identifier.
     *
     * @return the tile object, or null if no such tile exists.
     */
    public Tile getTile (ImageManager imgmgr, int tid)
	throws NoSuchTileException;
}
