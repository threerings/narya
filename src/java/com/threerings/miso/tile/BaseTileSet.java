//
// $Id: BaseTileSet.java,v 1.6 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Image;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.SwissArmyTileSet;

/**
 * The miso tile set extends the swiss army tile set to add support for
 * tile passability. Passability is used to determine whether {@link
 * com.threerings.miso.scene.Traverser} objects can traverse a particular
 * tile in a scene.
 */
public class MisoTileSet extends SwissArmyTileSet
{
    /**
     * Sets the passability information for the tiles in this tileset.
     * Each entry in the array corresponds to the tile at that tile index.
     */
    public void setPassability (boolean[] passable)
    {
        _passable = passable;
    }

    // documentation inherited
    public Tile createTile (Image image, int tileIndex)
    {
	return new MisoTile(image, _passable[tileIndex]);
    }

    /** Whether each tile is passable. */
    protected boolean[] _passable;
}
