//
// $Id: BaseTile.java,v 1.1 2001/10/08 21:04:25 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.tile.Tile;

/**
 * The miso tile class extends the base tile class to add support for
 * tile passability.
 *
 * @see MisoTileSet
 */
public class MisoTile extends Tile
{
    /** Whether the tile is passable. */
    public boolean passable;

    public MisoTile (int tsid, int tid)
    {
	super(tsid, tid);
    }
}
