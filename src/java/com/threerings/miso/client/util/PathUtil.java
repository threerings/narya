//
// $Id: PathUtil.java,v 1.1 2001/08/14 21:29:40 shaper Exp $

package com.threerings.miso.scene.util;

import java.util.ArrayList;

import com.threerings.miso.Log;
import com.threerings.miso.tile.Tile;

/**
 * The <code>PathUtil</code> class provides utility routines for
 * finding a reasonable path between two points in a scene.
 */
public class PathUtil
{
    /**
     * Return a list of <code>Point</code> objects representing a path
     * from coordinates (ax, by) to (bx, by), inclusive, determined by
     * performing an A* search in the given array of tiles.
     *
     * @param tiles the tile array.
     * @param ax the starting x-position in tile coordinates.
     * @param ay the starting y-position in tile coordinates.
     * @param bx the ending x-position in tile coordinates.
     * @param by the ending y-position in tile coordinates.
     *
     * @return the list of points in the path.
     */
    public static ArrayList getAStarPath (
	Tile tiles[][], int ax, int ay, int bx, int by)
    {
	return null;
    }
}
