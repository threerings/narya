//
// $Id: MisoTileSetImpl.java,v 1.1 2001/10/12 00:43:04 shaper Exp $

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
public class MisoTileSetImpl
    extends TileSetImpl
    implements MisoTileSet
{
    /** The miso scene layer the tiles are intended for. */
    public int layer;

    /** Whether each tile is passable. */
    public int passable[];

    // documentation inherited
    public Tile createTile (int tid)
    {
	// only create miso tiles for the base layer
	if (layer != MisoScene.LAYER_BASE) {
	    return super.createTile(tid);
	}

	return new MisoTile(tsid, tid);
    }

    // documentation inherited
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

    // documentation inherited
    public int getLayerIndex ()
    {
        return layer;
    }
}
