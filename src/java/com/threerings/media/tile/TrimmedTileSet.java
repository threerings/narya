//
// $Id: TrimmedTileSet.java,v 1.1 2002/05/06 18:08:32 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Rectangle;

/**
 * Contains the necessary information to create a set of trimmed tiles
 * from a base image and the associated trim metrics.
 */
public class TrimmedTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
        return _obounds.length;
    }

    /**
     * Provides this tileset with access to the trimmed tile metrics. This
     * generally only is called when generating a trimmed tileset from a
     * regular tileset.
     */
    public void setTileMetrics (Rectangle[] obounds, Rectangle[] tbounds)
    {
        _obounds = obounds;
        _tbounds = tbounds;
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex, Image tilesetImage)
    {
        // N/A
        return null;
    }

    // documentation inherited
    protected Tile createTile (int tileIndex, Image tilesetImage)
    {
        return new TrimmedTile(
            tilesetImage, _obounds[tileIndex], _tbounds[tileIndex]);
    }

    /** The width and height of the untrimmed tile, and the x and y offset
     * of the trimmed image within our tileset image. */
    protected Rectangle[] _obounds;

    /** The width and height of the trimmed image and the x and y offset
     * within the untrimmed image at which the trimmed image should be
     * rendered. */
    protected Rectangle[] _tbounds;
}
