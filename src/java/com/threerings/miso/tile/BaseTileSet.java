//
// $Id: BaseTileSet.java,v 1.11 2003/01/13 22:55:12 mdb Exp $

package com.threerings.miso.tile;

import com.samskivert.util.StringUtil;

import com.threerings.media.image.Mirage;
import com.threerings.media.tile.SwissArmyTileSet;
import com.threerings.media.tile.Tile;

/**
 * The base tileset extends the swiss army tileset to add support for tile
 * passability. Passability is used to determine whether traverser objects
 * (generally sprites made to "walk" around the scene) can traverse a
 * particular tile in a scene.
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
    protected Tile createTile (int tileIndex, Mirage tileImage)
    {
        return new BaseTile(tileImage, _passable[tileIndex]);
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
