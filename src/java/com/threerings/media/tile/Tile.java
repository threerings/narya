//
// $Id: Tile.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.tile;

import java.awt.Image;

/**
 * A tile represents a single square in a single layer in a scene.
 */
public class Tile
{
    public Image img;  // the tile image
    public short tsid; // the tile set identifier
    public short tid;  // the tile identifier within the set

    // height and width of a tile image in pixels
    public static final int HEIGHT = 16;
    public static final int WIDTH = 32;

    // halved values of tile width/height in pixels for use in common
    // tile-dimension-related calculations
    public static final int HALF_HEIGHT = HEIGHT / 2;
    public static final int HALF_WIDTH = WIDTH / 2;

    /**
     * Construct a new tile with the specified identifiers.  Intended
     * only for use by the TileManager.  Do not use this method.
     *
     * @see com.threerings.cocktail.miso.TileManager#getTile
     */
    public Tile (short tsid, short tid)
    {
	this.tsid = tsid;
	this.tid = tid;
    }

    /**
     * Construct a new tile with the specified identifiers.  Intended
     * only for use by the TileManager.  Do not use this method.
     *
     * @see com.threerings.cocktail.miso.TileManager#getTile
     */
    public Tile (int tsid, int tid)
    {
	this.tsid = (short) tsid;
	this.tid = (short) tid;
    }
}
