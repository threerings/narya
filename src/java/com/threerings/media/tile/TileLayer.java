//
// $Id: TileLayer.java,v 1.1 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

/**
 * The tile layer class is a convenience class provided to simplify the
 * management of a two-dimensional array of tiles. It takes care of
 * dereferencing the tile array efficiently with methods that the Java
 * compiler should inline, and prevents the caller from having to do the
 * indexing multiplication by hand every time.
 */
public final class TileLayer
{
    /**
     * Constructs a tile layer instance with the supplied tiles, width and
     * height. The tiles should exist in row-major format (the first row
     * of tiles followed by the second and so on).
     *
     * @exception IllegalArgumentException thrown if the size of the tiles
     * array does not match the specified width and height.
     */
    public TileLayer (Tile[] tiles, int width, int height)
    {
        // sanity check
        if (tiles.length != width*height) {
            String errmsg = "tiles.length != width*height";
            throw new IllegalArgumentException(errmsg);
        }

        _tiles = tiles;
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
    public Tile getTile (int column, int row)
    {
        return _tiles[row*_width+column];
    }

    /**
     * Sets the tile at the specified row and column. Bounds checking is
     * not done. Note that the parameters are column first, followed by
     * row (x, y order rather than row, column order).
     */
    public void setTile (int column, int row, Tile tile)
    {
        _tiles[row*_width+column] = tile;
    }

    /** Our tiles array. */
    private Tile[] _tiles;

    /** The number of tiles in a row. */
    private int _width;

    /** The number of rows. */
    private int _height;
}
