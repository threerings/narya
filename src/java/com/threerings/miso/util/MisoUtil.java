//
// $Id: MisoUtil.java,v 1.20 2003/04/18 22:59:04 mdb Exp $

package com.threerings.miso.util;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import com.samskivert.swing.SmartPolygon;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.MathUtil;

import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;

import com.threerings.miso.Log;

/**
 * Miscellaneous isometric-display-related utility routines.
 */
public class MisoUtil
    implements DirectionCodes
{
    /**
     * Given two points in screen pixel coordinates, return the
     * compass direction that point B lies in from point A from an
     * isometric perspective.
     *
     * @param ax the x-position of point A.
     * @param ay the y-position of point A.
     * @param bx the x-position of point B.
     * @param by the y-position of point B.
     *
     * @return the direction specified as one of the <code>Sprite</code>
     *         class's direction constants.
     */
    public static int getDirection (
        MisoSceneMetrics metrics, int ax, int ay, int bx, int by)
    {
        Point afpos = new Point(), bfpos = new Point();

        // convert screen coordinates to full coordinates to get both
        // tile coordinates and fine coordinates
        screenToFull(metrics, ax, ay, afpos);
        screenToFull(metrics, bx, by, bfpos);

        // pull out the tile coordinates for each point
        int tax = afpos.x / FULL_TILE_FACTOR;
        int tay = afpos.y / FULL_TILE_FACTOR;

        int tbx = bfpos.x / FULL_TILE_FACTOR;
        int tby = bfpos.y / FULL_TILE_FACTOR;

        // compare tile coordinates to determine direction
        int dir = getIsoDirection(tax, tay, tbx, tby);
        if (dir != DirectionCodes.NONE) {
            return dir;
        }

        // destination point is in the same tile as the
        // origination point, so consider fine coordinates

        // pull out the fine coordinates for each point
        int fax = afpos.x - (tax * FULL_TILE_FACTOR);
        int fay = afpos.y - (tay * FULL_TILE_FACTOR);

        int fbx = bfpos.x - (tbx * FULL_TILE_FACTOR);
        int fby = bfpos.y - (tby * FULL_TILE_FACTOR);

        // compare fine coordinates to determine direction
        dir = getIsoDirection(fax, fay, fbx, fby);

        // arbitrarily return southwest if fine coords were also equivalent
        return (dir == -1) ? SOUTHWEST : dir;
    }

    /**
     * Given two points in an isometric coordinate system (in which {@link
     * #NORTH} is in the direction of the negative x-axis and {@link
     * #WEST} in the direction of the negative y-axis), return the compass
     * direction that point B lies in from point A.  This method is used
     * to determine direction for both tile coordinates and fine
     * coordinates within a tile, since the coordinate systems are the
     * same.
     *
     * @param ax the x-position of point A.
     * @param ay the y-position of point A.
     * @param bx the x-position of point B.
     * @param by the y-position of point B.
     *
     * @return the direction specified as one of the <code>Sprite</code>
     * class's direction constants, or <code>DirectionCodes.NONE</code> if
     * point B is equivalent to point A.
     */
    public static int getIsoDirection (int ax, int ay, int bx, int by)
    {
        // head off a div by 0 at the pass..
        if (bx == ax) {
            if (by == ay) {
                return DirectionCodes.NONE;
            }
            return (by < ay) ? EAST : WEST;
        }

        // figure direction base on the slope of the line
        float slope = ((float) (ay - by)) / ((float) Math.abs(ax - bx));
        if (slope > 2f) {
            return EAST;
        }
        if (slope > .5f) {
            return (bx < ax) ? NORTHEAST : SOUTHEAST;
        }
        if (slope > -.5f) {
            return (bx < ax) ? NORTH : SOUTH;
        }
        if (slope > -2f) {
            return (bx < ax) ? NORTHWEST : SOUTHWEST;
        }
        return WEST;
    }

    /**
     * Given two points in screen coordinates, return the isometrically
     * projected compass direction that point B lies in from point A.
     *
     * @param ax the x-position of point A.
     * @param ay the y-position of point A.
     * @param bx the x-position of point B.
     * @param by the y-position of point B.
     *
     * @return the direction specified as one of the <code>Sprite</code>
     * class's direction constants, or <code>DirectionCodes.NONE</code> if
     * point B is equivalent to point A.
     */
    public static int getProjectedIsoDirection (int ax, int ay, int bx, int by)
    {
        return toIsoDirection(DirectionUtil.getDirection(ax, ay, bx, by));
    }

    /**
     * Converts a non-isometric orientation (where north points toward the
     * top of the screen) to an isometric orientation where north points
     * toward the upper-left corner of the screen.
     */
    public static int toIsoDirection (int dir)
    {
        if (dir != DirectionCodes.NONE) {
            // rotate the direction clockwise (ie. change SOUTHEAST to
            // SOUTH)
            dir = DirectionUtil.rotateCW(dir, 2);
        }
        return dir;
    }

    /**
     * Returns the tile coordinate of the given full coordinate.
     */
    public static int fullToTile (int val)
    {
        return (val / FULL_TILE_FACTOR);
    }

    /**
     * Returns the fine coordinate of the given full coordinate.
     */
    public static int fullToFine (int val)
    {
        return (val - ((val / FULL_TILE_FACTOR) * FULL_TILE_FACTOR));
    }

    /**
     * Convert the given screen-based pixel coordinates to their
     * corresponding tile-based coordinates.  Converted coordinates
     * are placed in the given point object.
     *
     * @param sx the screen x-position pixel coordinate.
     * @param sy the screen y-position pixel coordinate.
     * @param tpos the point object to place coordinates in.
     *
     * @return the point instance supplied via the <code>tpos</code>
     * parameter.
     */
    public static Point screenToTile (
        MisoSceneMetrics metrics, int sx, int sy, Point tpos)
    {
        // determine the upper-left of the quadrant that contains our
        // point
        int zx = (int)Math.floor((float)(sx - metrics.origin.x) /
                                 metrics.tilewid);
        int zy = (int)Math.floor((float)(sy - metrics.origin.y) /
                                 metrics.tilehei);

        // these are the screen coordinates of the tile's top
        int ox = (zx * metrics.tilewid + metrics.origin.x),
            oy = (zy * metrics.tilehei + metrics.origin.y);

        // these are the tile coordinates
        tpos.x = zy + zx; tpos.y = zy - zx;

        // now determine which of the four tiles our point occupies
        int dx = sx - ox, dy = sy - oy;

        if (Math.round(metrics.slopeY * dx + metrics.tilehei) <= dy) {
            tpos.x += 1;
        }

        if (Math.round(metrics.slopeX * dx) > dy) {
            tpos.y -= 1;
        }

//         Log.info("Converted [sx=" + sx + ", sy=" + sy +
//                  ", zx=" + zx + ", zy=" + zy +
//                  ", ox=" + ox + ", oy=" + oy +
//                  ", dx=" + dx + ", dy=" + dy +
//                  ", tpos.x=" + tpos.x + ", tpos.y=" + tpos.y + "].");
        return tpos;
    }

    /**
     * Convert the given tile-based coordinates to their corresponding
     * screen-based pixel coordinates. The screen coordinate for a tile is
     * the upper-left coordinate of the rectangle that bounds the tile
     * polygon. Converted coordinates are placed in the given point
     * object.
     *
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     * @param spos the point object to place coordinates in.
     *
     * @return the point instance supplied via the <code>spos</code>
     * parameter.
     */
    public static Point tileToScreen (
        MisoSceneMetrics metrics, int x, int y, Point spos)
    {
        spos.x = metrics.origin.x + ((x - y - 1) * metrics.tilehwid);
        spos.y = metrics.origin.y + ((x + y) * metrics.tilehhei);
        return spos;
    }

    /**
     * Convert the given fine coordinates to pixel coordinates within
     * the containing tile.  Converted coordinates are placed in the
     * given point object.
     *
     * @param x the x-position fine coordinate.
     * @param y the y-position fine coordinate.
     * @param ppos the point object to place coordinates in.
     */
    public static void fineToPixel (
        MisoSceneMetrics metrics, int x, int y, Point ppos)
    {
        ppos.x = metrics.tilehwid + ((x - y) * metrics.finehwid);
        ppos.y = (x + y) * metrics.finehhei;
    }

    /**
     * Convert the given pixel coordinates, whose origin is at the
     * top-left of a tile's containing rectangle, to fine coordinates
     * within that tile.  Converted coordinates are placed in the
     * given point object.
     *
     * @param x the x-position pixel coordinate.
     * @param y the y-position pixel coordinate.
     * @param fpos the point object to place coordinates in.
     */
    public static void pixelToFine (
        MisoSceneMetrics metrics, int x, int y, Point fpos)
    {
        // calculate line parallel to the y-axis (from the given
        // x/y-pos to the x-axis)
        float bY = y - (metrics.fineSlopeY * x);

        // determine intersection of x- and y-axis lines
        int crossx = (int)((bY - metrics.fineBX) /
                           (metrics.fineSlopeX - metrics.fineSlopeY));
        int crossy = (int)((metrics.fineSlopeY * crossx) + bY);

        // TODO: final position should check distance between our
        // position and the surrounding fine coords and return the
        // actual closest fine coord, rather than just dividing.

        // determine distance along the x-axis
        float xdist = MathUtil.distance(metrics.tilehwid, 0, crossx, crossy);
        fpos.x = (int)(xdist / metrics.finelen);

        // determine distance along the y-axis
        float ydist = MathUtil.distance(x, y, crossx, crossy);
        fpos.y = (int)(ydist / metrics.finelen);
    }

    /**
     * Convert the given screen-based pixel coordinates to full
     * scene-based coordinates that include both the tile coordinates
     * and the fine coordinates in each dimension.  Converted
     * coordinates are placed in the given point object.
     *
     * @param sx the screen x-position pixel coordinate.
     * @param sy the screen y-position pixel coordinate.
     * @param fpos the point object to place coordinates in.
     *
     * @return the point passed in to receive the coordinates.
     */
    public static Point screenToFull (
        MisoSceneMetrics metrics, int sx, int sy, Point fpos)
    {
        // get the tile coordinates
        Point tpos = new Point();
        screenToTile(metrics, sx, sy, tpos);

        // get the screen coordinates for the containing tile
        Point spos = tileToScreen(metrics, tpos.x, tpos.y, new Point());

        // get the fine coordinates within the containing tile
        pixelToFine(metrics, sx - spos.x, sy - spos.y, fpos);

        // toss in the tile coordinates for good measure
        fpos.x += (tpos.x * FULL_TILE_FACTOR);
        fpos.y += (tpos.y * FULL_TILE_FACTOR);

        return fpos;
    }

    /**
     * Convert the given full coordinates to screen-based pixel
     * coordinates.  Converted coordinates are placed in the given
     * point object.
     *
     * @param x the x-position full coordinate.
     * @param y the y-position full coordinate.
     * @param spos the point object to place coordinates in.
     *
     * @return the point passed in to receive the coordinates.
     */
    public static Point fullToScreen (
        MisoSceneMetrics metrics, int x, int y, Point spos)
    {
        // get the tile screen position
        int tx = x / FULL_TILE_FACTOR, ty = y / FULL_TILE_FACTOR;
        Point tspos = tileToScreen(metrics, tx, ty, new Point());

        // get the pixel position of the fine coords within the tile
        Point ppos = new Point();
        int fx = x - (tx * FULL_TILE_FACTOR), fy = y - (ty * FULL_TILE_FACTOR);
        fineToPixel(metrics, fx, fy, ppos);

        // final position is tile position offset by fine position
        spos.x = tspos.x + ppos.x;
        spos.y = tspos.y + ppos.y;

        return spos;
    }

    /**
     * Converts the given fine coordinate to a full coordinate (a tile
     * coordinate plus a fine coordinate remainder). The fine coordinate
     * is assumed to be relative to tile <code>(0, 0)</code>.
     */
    public static int fineToFull (MisoSceneMetrics metrics, int fine)
    {
        return toFull(fine / metrics.finegran, fine % metrics.finegran);
    }

    /**
     * Composes the supplied tile coordinate and fine coordinate offset
     * into a full coordinate.
     */
    public static int toFull (int tile, int fine)
    {
        return tile * FULL_TILE_FACTOR + fine;
    }

    /**
     * Return a polygon framing the specified tile.
     *
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     */
    public static Polygon getTilePolygon (
        MisoSceneMetrics metrics, int x, int y)
    {
        return getFootprintPolygon(metrics, x, y, 1, 1);
    }

    /**
     * Return a screen-coordinates polygon framing the two specified
     * tile-coordinate points.
     */
    public static Polygon getMultiTilePolygon (MisoSceneMetrics metrics,
                                               Point sp1, Point sp2)
    {
        int x = Math.min(sp1.x, sp2.x), y = Math.min(sp1.y, sp2.y);
        int width = Math.abs(sp1.x-sp2.x)+1, height = Math.abs(sp1.y-sp2.y)+1;
        return getFootprintPolygon(metrics, x, y, width, height);
    }

    /**
     * Returns a polygon framing the specified scene footprint.
     *
     * @param x the x tile coordinate of the "upper-left" of the footprint.
     * @param y the y tile coordinate of the "upper-left" of the footprint.
     * @param width the width in tiles of the footprint.
     * @param height the height in tiles of the footprint.
     */
    public static Polygon getFootprintPolygon (
        MisoSceneMetrics metrics, int x, int y, int width, int height)
    {
        SmartPolygon footprint = new SmartPolygon();
        Point tpos = MisoUtil.tileToScreen(metrics, x, y, new Point());

        // start with top-center point
        int rx = tpos.x + metrics.tilehwid, ry = tpos.y;
        footprint.addPoint(rx, ry);
        // right point
        rx += width * metrics.tilehwid;
        ry += width * metrics.tilehhei;
        footprint.addPoint(rx, ry);
        // bottom-center point
        rx -= height * metrics.tilehwid;
        ry += height * metrics.tilehhei;
        footprint.addPoint(rx, ry);
        // left point
        rx -= width * metrics.tilehwid;
        ry -= width * metrics.tilehhei;
        footprint.addPoint(rx, ry);
        // end with top-center point
        rx += height * metrics.tilehwid;
        ry -= height * metrics.tilehhei;
        footprint.addPoint(rx, ry);

        return footprint;
    }

    /**
     * Adds the supplied fine coordinates to the supplied tile coordinates
     * to compute full coordinates.
     *
     * @retun the point object supplied as <code>full</code>.
     */
    public static Point tilePlusFineToFull (MisoSceneMetrics metrics,
                                            int tileX, int tileY,
                                            int fineX, int fineY,
                                            Point full)
    {
        int dtx = fineX / metrics.finegran;
        int dty = fineY / metrics.finegran;
        int fx = fineX - dtx * metrics.finegran;
        if (fx < 0) {
            dtx--;
            fx += metrics.finegran;
        }
        int fy = fineY - dty * metrics.finegran;
        if (fy < 0) {
            dty--;
            fy += metrics.finegran;
        }

        full.x = toFull(tileX + dtx, fx);
        full.y = toFull(tileY + dty, fy);
        return full;
    }

    /**
     * Turns x and y scene coordinates into an integer key.
     *
     * @return the hash key, given x and y.
     */
    public static final int coordsToKey (int x, int y)
    {
        return ((y << 16) & (0xFFFF0000)) | (x & 0xFFFF);
    }

    /**
     * Gets the x coordinate from an integer hash key.
     *
     * @return the x coordinate.
     */
    public static final int xCoordFromKey (int key)
    {
        return (key & 0xFFFF);
    }

    /**
     * Gets the y coordinate from an integer hash key.
     *
     * @return the y coordinate from the hash key.
     */
    public static final int yCoordFromKey (int key)
    {
        return ((key >> 16) & 0xFFFF);
    }

    /** Multiplication factor to embed tile coords in full coords. */
    protected static final int FULL_TILE_FACTOR = 100;
}
