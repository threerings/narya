//
// $Id: BaseTileSet.java,v 1.5 2001/11/08 03:04:45 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Point;

import com.samskivert.util.HashIntMap;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.miso.scene.MisoScene;

/**
 * The miso tile set extends the swiss army tile set to add support for
 * tile passability. Passability is used to determine whether {@link
 * com.threerings.miso.scene.Traverser} objects can traverse a particular
 * tile in a {@link MisoScene}.
 */
public class MisoTileSet extends SwissArmyTileSet
{
    /**
     * Constructs a Miso tileset with the swiss army tile set
     * configuration information and additional information about tile
     * passability.
     *
     * @param layer the layer to which this tileset is assigned.
     * @param passable info on each tile indicating whether or not the
     * tile is passable (can be walked on by sprites).
     *
     * @see SwissArmyTileSet#SwissArmyTileSet
     */
    public MisoTileSet (
        ImageManager imgmgr, String imgFile, String name, int tsid,
        int[] tileCount, int[] rowWidth, int[] rowHeight,
        Point offsetPos, Point gapDist, int layer, int[] passable)
    {
        super(imgmgr, imgFile, name, tsid, tileCount,
              rowWidth, rowHeight, offsetPos, gapDist);

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
