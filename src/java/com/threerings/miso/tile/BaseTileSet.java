//
// $Id: BaseTileSet.java,v 1.14 2004/08/27 02:20:07 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.tile;

import com.samskivert.util.StringUtil;

import com.threerings.media.image.Colorization;
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
    protected Tile createTile ()
    {
        return new BaseTile();
    }

    // documentation inherited
    protected void initTile (Tile tile, int tileIndex, Colorization[] zations)
    {
        super.initTile(tile, tileIndex, zations);
        ((BaseTile)tile).setPassable(_passable[tileIndex]);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", passable=").append(StringUtil.toString(_passable));
    }

    /** Whether each tile is passable. */
    protected boolean[] _passable;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
