//
// $Id: TileUtil.java,v 1.3 2002/05/06 18:08:32 mdb Exp $

package com.threerings.media.tile;

import com.threerings.media.Log;

/**
 * Miscellaneous utility routines for working with tiles.
 */
public class TileUtil
{
    /**
     * Returns the image associated with the given tile from the given
     * tile manager, or null if an error occurred.  Any exceptions that
     * occur are logged.
     *
     * @param tilemgr the tile manager via which the tile should be
     * fetched.
     * @param tileSetId the id of the tileset from which to fetch the
     * tile.
     * @param tileIndex the index of the tile to be fetched.
     */
    public static Tile getTile (
        TileManager tilemgr, int tileSetId, int tileIndex)
    {
	try {
            TileSet set = tilemgr.getTileSet(tileSetId);
            return set.getTile(tileIndex);

	} catch (TileException te) {
	    Log.warning("Error retrieving tile [tsid=" + tileSetId +
                        ", tidx=" + tileIndex + ", error=" + te + "].");
	    return null;
	}
    }

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
