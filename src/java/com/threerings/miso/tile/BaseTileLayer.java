//
// $Id: BaseTileLayer.java,v 1.2 2001/11/18 04:27:56 mdb Exp $

package com.threerings.miso.tile;

/**
 * The miso tile layer class is a convenience class provided to simplify
 * the management of a two-dimensional array of miso tiles. It takes care
 * of dereferencing the tile array efficiently with methods that the Java
 * compiler should inline, and prevents the caller from having to do the
 * indexing multiplication by hand every time.
 *
 * <p> This is equivalent to {@link com.threerings.media.tile.TileLayer}
 * except that it contains {@link MisoTile} instances. For efficiency's
 * sake, we don't extend that class but instead provide a direct
 * implementation.
 */
public final class MisoTileLayer
{
    /**
     * Constructs a miso tile layer instance with the supplied tiles,
     * width and height. The tiles should exist in row-major format (the
     * first row of tiles followed by the second and so on).
     *
     * @exception IllegalArgumentException thrown if the size of the tiles
     * array does not match the specified width and height.
     */
    public MisoTileLayer (MisoTile[] tiles, int width, int height)
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
    public MisoTile getTile (int column, int row)
    {
        return _tiles[row*_width+column];
    }

    /**
     * Sets the tile at the specified row and column. Bounds checking is
     * not done. Note that the parameters are column first, followed by
     * row (x, y order rather than row, column order).
     */
    public void setTile (int column, int row, MisoTile tile)
    {
        _tiles[row*_width+column] = tile;
    }

    /** Our tiles array. */
    private MisoTile[] _tiles;

    /** The number of tiles in a row. */
    private int _width;

    /** The number of rows. */
    private int _height;
}
