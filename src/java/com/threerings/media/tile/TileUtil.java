//
// $Id: TileUtil.java,v 1.5 2003/02/12 05:32:35 mdb Exp $

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
