//
// $Id: Tile.java,v 1.16 2001/10/22 18:11:25 shaper Exp $

package com.threerings.media.tile;

import java.awt.*;

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
     * Returns the fully qualified tile id for this tile. The fully
     * qualified id contains both the tile set identifier and the tile
     * identifier.
     */
    public int getTileId ()
    {
        return ((int)tsid << 16) | tid;
    }

    /**
     * Render the tile image at the top-left corner of the given shape
     * in the given graphics context.
     */
    public void paint (Graphics2D gfx, Shape dest)
    {
	Rectangle bounds = dest.getBounds();
	gfx.drawImage(img, bounds.x, bounds.y, null);
    }

    /**
     * Return a string representation of the tile information.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[tsid=").append(tsid);
	buf.append(", tid=").append(tid);
	return buf.append("]").toString();
    }
}
