//
// $Id: Tile.java,v 1.7 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Image;

/**
 * A tile represents a single square in a single layer in a scene.
 */
public class Tile
{
    /** The tile image. */
    public Image img;

    /** The tile set identifier. */
    public short tsid;

    /** The tile identifier within the set. */
    public short tid;

    /** The tile height in pixels. */
    public short height; // the tile height in pixels

    /** The height and width of a tile image in pixels. */
    public static final int HEIGHT = 16;
    public static final int WIDTH = 32;

    /** Halved tile width in pixels for use in common calculations. */
    public static final int HALF_HEIGHT = HEIGHT / 2;

    /** Halved tile height in pixels for use in common calculations. */
    public static final int HALF_WIDTH = WIDTH / 2;

    /**
     * Construct a new tile with the specified identifiers.  Intended
     * only for use by the TileManager.  Do not use this method.
     *
     * @see TileManager#getTile
     */
    public Tile (int tsid, int tid)
    {
	this.tsid = (short) tsid;
	this.tid = (short) tid;
    }

    /**
     * Return a string representation of the tile information.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[tsid=").append(tsid);
	buf.append(", tid=").append(tid);
	buf.append(", img=").append(img);
	return buf.append("]").toString();
    }
}
