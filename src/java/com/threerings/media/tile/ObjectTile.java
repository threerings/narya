//
// $Id: ObjectTile.java,v 1.3 2001/11/01 01:40:42 shaper Exp $

package com.threerings.media.tile;

import java.awt.*;

/**
 * An object tile extends the base tile to provide support for objects
 * whose image spans more than one tile.  An object tile has
 * dimensions that represent its footprint or "shadow", which the
 * scene containing the tile can then reference to do things like
 * making the footprint tiles impassable.
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
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", baseWidth=").append(baseWidth);
        buf.append(", baseHeight=").append(baseHeight);
    }
}
