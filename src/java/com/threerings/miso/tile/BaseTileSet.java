//
// $Id: BaseTileSet.java,v 1.4 2001/11/01 01:40:42 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Point;

import com.samskivert.util.HashIntMap;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.miso.scene.MisoScene;

/**
 * The miso tile set extends the base tile set to add support for tile
 * passability.  Passability is used to determine whether {@link
 * com.threerings.miso.scene.Traverser} objects can traverse a
 * particular tile in a {@link com.threerings.miso.scene.MisoScene}.
 */
public class MisoTileSet extends TileSet
{
    public MisoTileSet (
        ImageManager imgmgr, int tsid, String name, String imgFile,
        int tileCount[], int rowWidth[], int rowHeight[],
        int numTiles, Point offsetPos, Point gapDist,
        boolean isObjectSet, HashIntMap objects,
        int layer, int passable[])
    {
        super(imgmgr, tsid, name, imgFile, tileCount, rowWidth,
              rowHeight, numTiles, offsetPos, gapDist,
              isObjectSet, objects);

        _layer = layer;
        _passable = passable;
    }

    // documentation inherited
    public Tile createTile (int tid)
    {
	// only create miso tiles for the base layer
	if (_layer != MisoScene.LAYER_BASE) {
	    return super.createTile(tid);
	}

	return new MisoTile(_tsid, tid);
    }

    // documentation inherited
    protected void populateTile (Tile tile)
    {
	super.populateTile(tile);

	if (tile instanceof MisoTile) {
	    // set the tile's passability, defaulting to passable if this
	    // tileset has no passability specified
	    ((MisoTile)tile).passable =
		(_passable == null || (_passable[tile.tid] == 1));
	}
    }

    // documentation inherited
    public int getLayerIndex ()
    {
        return _layer;
    }

    /** The miso scene layer the tiles are intended for. */
    protected int _layer;

    /** Whether each tile is passable. */
    protected int _passable[];
}
