//
// $Id: BaseTileSet.java,v 1.1 2001/10/08 21:04:25 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.tile.*;

/**
 * The miso tile set class extends the base tile set class to add
 * support for tile passability.  Passability is used to determine
 * whether {@link com.threerings.miso.scene.Traverser} objects can
 * traverse a particular tile in a {@link
 * com.threerings.miso.scene.MisoScene}.
 */
public class MisoTileSet extends TileSet
{
    public MisoTileSet ()
    {
	_model = new MisoTileSetModel();
    }

    public Tile createTile (int tid)
    {
	MisoTile tile = new MisoTile(_model.tsid, tid);

	// set the tile's passability, defaulting to passable if this
	// tileset has no passability specified
	int passable[] = ((MisoTileSetModel)_model).passable;
	tile.passable = (passable == null || (passable[tid] == 1));

	return tile;
    }

    protected static class MisoTileSetModel extends TileSetModel
    {
	/** Whether each tile is passable. */
	public int passable[];
    }
}
