//
// $Id: BaseTileSet.java,v 1.8 2001/11/29 00:17:32 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Image;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.SwissArmyTileSet;

/**
 * The base tileset extends the swiss army tileset to add support for tile
 * passability. Passability is used to determine whether {@link
 * com.threerings.miso.scene.Traverser} objects can traverse a particular
 * tile in a scene.
 */
public class BaseTileSet extends SwissArmyTileSet
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
    protected Tile createTile (int tileIndex, Image image)
    {
	return new BaseTile(image, _passable[tileIndex]);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", passable=").append(StringUtil.toString(_passable));
    }

    /** Whether each tile is passable. */
    protected boolean[] _passable;
}
