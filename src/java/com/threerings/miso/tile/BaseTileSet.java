//
// $Id: BaseTileSet.java,v 1.2 2001/10/11 00:41:27 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.tile.*;

import com.threerings.miso.scene.MisoScene;

/**
 * The miso tile set class extends the base tile set class to add
 * support for tile passability.  Passability is used to determine
 * whether {@link com.threerings.miso.scene.Traverser} objects can
 * traverse a particular tile in a {@link
 * com.threerings.miso.scene.MisoScene}.
 */
public class MisoTileSet extends TileSetImpl
{
    /** The miso scene layer the tiles are intended for. */
    public int layer;

    /** Whether each tile is passable. */
    public int passable[];

    public Tile createTile (int tid)
    {
	// only create miso tiles for the base layer
	if (layer != MisoScene.LAYER_BASE) {
	    return super.createTile(tid);
	}

	return new MisoTile(tsid, tid);
    }

    protected void populateTile (Tile tile)
    {
	super.populateTile(tile);

	if (tile instanceof MisoTile) {
	    // set the tile's passability, defaulting to passable if this
	    // tileset has no passability specified
	    ((MisoTile)tile).passable =
		(passable == null || (passable[tile.tid] == 1));
	}
    }
}
