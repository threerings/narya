//
// $Id: ObjectTile.java,v 1.1 2001/10/11 00:41:26 shaper Exp $

package com.threerings.media.tile;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * An object tile extends the base tile to provide support for objects
 * whose image spans more than one tile.  The object has dimensions
 * that represent its footprint or "shadow", which the scene
 * containing the tile can reference to do things like make the
 * footprint tiles impassable.
 */
public class ObjectTile extends Tile
{
    /** The object footprint dimensions in unit tile units. */
    public int baseWidth, baseHeight;

    /**
     * Constructs a new object tile.
     */
    public ObjectTile (int tsid, int tid, int baseWidth, int baseHeight)
    {
	super(tsid, tid);

	this.baseWidth = baseWidth;
	this.baseHeight = baseHeight;
    }

    // documentation inherited
    public void paint (Graphics2D gfx, Shape dest)
    {
	super.paint(gfx, dest);

	// TODO: draw the object image offset to account for its
	// actual dimensions
    }
}
