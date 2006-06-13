//
// $Id$
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

import com.threerings.media.tile.Tile;

/**
 * Extends the tile class to add support for tile passability.
 *
 * @see BaseTileSet
 */
public class BaseTile extends Tile
{
    /**
     * Returns whether or not this tile can be walked upon by character
     * sprites.
     */
    public boolean isPassable ()
    {
        return _passable;
    }

    /**
     * Configures this base tile as passable or impassable.
     */
    public void setPassable (boolean passable)
    {
        _passable = passable;
    }

    /** Whether the tile is passable. */
    protected boolean _passable = true;
}
