//
// $Id: BaseTileSet.java,v 1.10 2002/05/06 18:08:32 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Image;
import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.SwissArmyTileSet;

import com.threerings.miso.scene.Traverser;

/**
 * The base tileset extends the swiss army tileset to add support for tile
 * passability. Passability is used to determine whether {@link Traverser}
 * objects can traverse a particular tile in a scene.
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

    /**
     * Returns the passability information for the tiles in this tileset.
     */
    public boolean[] getPassability ()
    {
        return _passable;
    }

    // documentation inherited
    protected Tile createTile (int tileIndex, Image tilesetImage)
    {
        return new BaseTile(tilesetImage,
                            computeTileBounds(tileIndex, tilesetImage),
                            _passable[tileIndex]);
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
