//
// $Id: TileUtil.java,v 1.6 2004/08/27 02:12:41 mdb Exp $
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

package com.threerings.media.tile;

/**
 * Miscellaneous utility routines for working with tiles.
 */
public class TileUtil
{
    /**
     * Generates a fully-qualified tile id given the supplied tileset id
     * and tile index.
     */
    public static int getFQTileId (int tileSetId, int tileIndex)
    {
        return (tileSetId << 16) | tileIndex;
    }

    /**
     * Extracts the tile set id from the supplied fully qualified tile id.
     */
    public static int getTileSetId (int fqTileId)
    {
        return (fqTileId >> 16);
    }

    /**
     * Extracts the tile index from the supplied fully qualified tile id.
     */
    public static int getTileIndex (int fqTileId)
    {
        return (fqTileId & 0xFFFF);
    }
}
