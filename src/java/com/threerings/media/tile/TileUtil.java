//
// $Id: TileUtil.java,v 1.2 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;

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
    public static Image getTileImage (
        TileManager tilemgr, int tileSetId, int tileIndex)
    {
	try {
            TileSet set = tilemgr.getTileSet(tileSetId);
            return set.getTileImage(tileIndex);

	} catch (TileException te) {
	    Log.warning("Error retrieving tile image [error=" + te + "].");
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
