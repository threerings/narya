//
// $Id: TileUtil.java,v 1.4 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

/**
 * Miscellaneous utility routines for working with tiles.
 */
public class TileUtil
{
    /**
     * Generates a fully-qualified tileid given the supplied tileset id
     * and tile index. This fully-qualified id can be used to fetch the
     * tile from the tileset repository which knows about the supplied
     * tileset id.
     */
    public static int getFQTileId (int tileSetId, int tileIndex)
    {
        return (tileSetId << 16) | tileIndex;
    }
}
