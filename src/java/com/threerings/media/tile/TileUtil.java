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

    /**
     * Compute some hash value for "randomizing" tileset picks
     * based on x and y coordinates.
     *
     * @return a positive, seemingly random number based on x and y.
     */
    public static int getTileHash (int x, int y)
    {
        long seed = ((x ^ y) ^ MULTIPLIER) & MASK;
        long hash = (seed * MULTIPLIER + ADDEND) & MASK;
        return (int) (hash >>> 30);
    }

    protected static final long MULTIPLIER = 0x5DEECE66DL;
    protected static final long ADDEND = 0xBL;
    protected static final long MASK = (1L << 48) - 1;
}
