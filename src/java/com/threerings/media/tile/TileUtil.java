//
// $Id: TileUtil.java,v 1.1 2001/10/11 00:41:26 shaper Exp $

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
     * tile manager, or null if an error occurred.  Any exceptions
     * that occur are logged.
     */
    public static Image getTileImage (TileManager tilemgr, int tsid, int tid)
    {
	try {
	    Tile tile = tilemgr.getTile(tsid, tid);
	    return tile.img;

	} catch (TileException te) {
	    Log.warning("Exception retrieving tile image [te=" + te + "].");
	    return null;
	}
    }

    /**
     * Returns the given tile from the given tile manager, or null if
     * an error occurred.  Any exceptions that occur are logged.
     */
    public static Tile getTile (TileManager tilemgr, int tsid, int tid)
    {
	try {
	    return tilemgr.getTile(tsid, tid);

	} catch (TileException te) {
	    Log.warning("Exception retrieving tile [te=" + te + "].");
	    return null;
	}
    }
}
