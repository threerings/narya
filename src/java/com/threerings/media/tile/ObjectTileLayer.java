//
// $Id: ObjectTileLayer.java,v 1.3 2002/03/26 23:35:01 ray Exp $

package com.threerings.media.tile;

import com.samskivert.util.HashIntMap;

import com.threerings.miso.scene.util.IsoUtil;

/**
 * The object tile layer class is a convenience class provided to simplify
 * the management of a two-dimensional array of object tiles. It takes
 * care of dereferencing the tile array efficiently with methods that the
 * Java compiler should inline, and prevents the caller from having to do
 * the indexing multiplication by hand every time.
 *
 * <p> This is equivalent to {@link TileLayer} except that it contains
 * {@link ObjectTile} instances. For efficiency's sake, we don't extend
 * that class but instead provide a direct implementation.
 */
public final class ObjectTileLayer
{
    /**
     * Constructs an object tile layer instance with the supplied tiles,
     * width and height. The tiles should exist in row-major format (the
     * first row of tiles followed by the second and so on).
     *
     * @exception IllegalArgumentException thrown if the size of the tiles
     * array does not match the specified width and height.
     */
    public ObjectTileLayer (int width, int height)
    {
        _width = width;
        _height = height;
    }

    /**
     * Returns the width of the layer in tiles.
     */
    public int getWidth ()
    {
        return _width;
    }

    /**
     * Returns the height of the layer in tiles.
     */
    public int getHeight ()
    {
        return _height;
    }

    /**
     * Fetches the tile at the specified row and column. Bounds checking
     * is not done. Note that the parameters are column first, followed by
     * row (x, y order rather than row, column order).
     */
    public ObjectTile getTile (int column, int row)
    {
        return (ObjectTile) _tiles.get(IsoUtil.coordsToKey(column, row));
    }

    /**
     * Sets the tile at the specified row and column. Bounds checking is
     * not done. Note that the parameters are column first, followed by
     * row (x, y order rather than row, column order).
     */
    public void setTile (int column, int row, ObjectTile tile)
    {
        _tiles.put(IsoUtil.coordsToKey(column, row), tile);
    }

    /**
     * Removes the tile at the specified row and column.
     */
    public void clearTile (int column, int row)
    {
        _tiles.remove(IsoUtil.coordsToKey(column, row));
    }

    /** Our tiles. */
    private HashIntMap _tiles = new HashIntMap();

    /** The number of tiles in a row. */
    private int _width;

    /** The number of rows. */
    private int _height;
}
