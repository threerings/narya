//
// $Id: Tile.java,v 1.10 2001/08/15 02:30:27 shaper Exp $

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

    /** The tile width in pixels. */
    public short width;

    /** The tile height in pixels. */
    public short height;

    /** Whether the tile is passable. */
    public boolean passable;

    /**
     * Construct a new tile with the specified identifiers.  Intended
     * only for use by the <code>TileSet</code>.  Do not call this
     * method.
     *
     * @see TileSet#getTile
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
