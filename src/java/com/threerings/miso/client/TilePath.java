//
// $Id: TilePath.java,v 1.17 2003/05/16 00:26:18 mdb Exp $

package com.threerings.miso.client;

import java.awt.Point;
import java.util.List;

import com.threerings.util.DirectionCodes;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LineSegmentPath;
import com.threerings.media.util.MathUtil;
import com.threerings.media.util.PathNode;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;

/**
 * The tile path represents a path of tiles through a scene.  The path is
 * traversed by treating each pair of connected tiles as a line segment.
 * Only ambulatory sprites can follow a tile path, and their tile
 * coordinates are updated as the path is traversed.
 */
public class TilePath extends LineSegmentPath
    implements DirectionCodes
{
    /**
     * Constructs a tile path.
     *
     * @param metrics the metrics for the scene the with which the path is
     * associated.
     * @param sprite the sprite to follow the path.
     * @param tiles the tiles to be traversed during the path.
     * @param destx the destination x-position in screen pixel
     * coordinates.
     * @param desty the destination y-position in screen pixel
     * coordinates.
     */
    public TilePath (MisoSceneMetrics metrics, Sprite sprite,
                     List tiles, int destx, int desty)
    {
	// constrain destination pixels to fine coordinates
	Point fpos = new Point();
	MisoUtil.screenToFull(metrics, destx, desty, fpos);

        // add the starting path node
        int sx = sprite.getX(), sy = sprite.getY();
        Point ipos = MisoUtil.screenToTile(metrics, sx, sy, new Point());
        addNode(sx, sy, NORTH);

	// TODO: make more visually appealing path segments from start to
	// second tile, and penultimate to ultimate tile

        // add all remaining path nodes excepting the last one
        Point prev = new Point(ipos.x, ipos.y);
        Point spos = new Point();
        int size = tiles.size();
        for (int ii = 1; ii < size - 1; ii++) {
            Point next = (Point)tiles.get(ii);

            // determine the direction from previous to next node
            int dir = MisoUtil.getIsoDirection(prev.x, prev.y, next.x, next.y);

            // determine the node's position in screen pixel coordinates
            MisoUtil.tileToScreen(metrics, next.x, next.y, spos);

            // add the node to the path, wandering through the middle
            // of each tile in the path for now
            int dsx = spos.x + metrics.tilehwid;
            int dsy = spos.y + metrics.tilehhei;
            addNode(dsx, dsy, dir);

            prev = next;
        }

        // get the final destination point's screen coordinates
        // constrained to the closest full coordinate
        MisoUtil.fullToScreen(metrics, fpos.x, fpos.y, spos);

        // get the tile coordinates for the final destination tile
        int tdestx = MisoUtil.fullToTile(fpos.x);
        int tdesty = MisoUtil.fullToTile(fpos.y);

        // get the facing direction for the final node
        int dir;
        if (prev.x == ipos.x && prev.y == ipos.y) {
            // if destination is within starting tile, direction is
            // determined by studying the fine coordinates
            dir = MisoUtil.getDirection(metrics, sx, sy, spos.x, spos.y);

        } else {
            // else it's based on the last tile we traversed
            dir = MisoUtil.getIsoDirection(prev.x, prev.y, tdestx, tdesty);
        }

    	// add the final destination path node
	addNode(spos.x, spos.y, dir);
    }

    /**
     * Returns the estimated number of millis that we'll be traveling
     * along this path.
     */
    public long getEstimTravelTime ()
    {
        return (long)(_estimPixels / _vel);
    }

    // documentation inherited
    public void addNode (int x, int y, int dir)
    {
        super.addNode(x, y, dir);
        if (_last == null) {
            _last = new Point();
        } else {
            _estimPixels += MathUtil.distance(_last.x, _last.y, x, y);
        }
        _last.setLocation(x, y);
    }

    /** Used to compute estimated travel time. */
    protected Point _last;

    /** Estimated pixels traveled. */
    protected int _estimPixels;
}
