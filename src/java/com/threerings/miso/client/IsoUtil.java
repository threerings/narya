//
// $Id: IsoUtil.java,v 1.1 2001/08/03 22:23:47 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;

import com.threerings.miso.sprite.Path;
import com.threerings.miso.util.MathUtil;

/**
 * The <code>IsoUtil</code> class is a holding place for miscellaneous
 * isometric-display-related utility routines.
 */
public class IsoUtil
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
     * @return the direction specified as one of the <code>Path</code>
     *         class's direction constants.
     */
    public static int getDirection (
	IsoSceneModel model, int ax, int ay, int bx, int by)
    {
	Point afpos = new Point(), bfpos = new Point();

	// convert screen coordinates to full coordinates to get both
	// tile coordinates and fine coordinates
	screenToFull(model, ax, ay, afpos);
	screenToFull(model, bx, by, bfpos);

	// pull out the tile coordinates for each point
	int tax = afpos.x / FULL_TILE_FACTOR;
	int tay = afpos.y / FULL_TILE_FACTOR;

	int tbx = bfpos.x / FULL_TILE_FACTOR;
	int tby = bfpos.y / FULL_TILE_FACTOR;

	// compare tile coordinates to determine direction
	int dir = getIsoDirection(model, tax, tay, tbx, tby);
	if (dir != Path.DIR_NONE) return dir;

	// destination point is in the same tile as the
	// origination point, so consider fine coordinates

	// pull out the fine coordinates for each point
	int fax = afpos.x - (tax * FULL_TILE_FACTOR);
	int fay = afpos.y - (tay * FULL_TILE_FACTOR);

	int fbx = bfpos.x - (tbx * FULL_TILE_FACTOR);
	int fby = bfpos.y - (tby * FULL_TILE_FACTOR);

	// compare fine coordinates to determine direction
	dir = getIsoDirection(model, fax, fay, fbx, fby);

	// arbitrarily return southwest if fine coords were also equivalent
	return (dir == -1) ? Path.DIR_SOUTHWEST : dir;
    }

    /**
     * Given two points in an isometric coordinate system, return the
     * compass direction that point B lies in from point A.  This
     * method is used to determine direction for both tile coordinates
     * and fine coordinates within a tile, since the coordinate
     * systems are the same.
     *
     * @param ax the x-position of point A.
     * @param ay the y-position of point A.
     * @param bx the x-position of point B.
     * @param by the y-position of point B.
     *
     * @return the direction specified as one of the <code>Path</code>
     *         class's direction constants, or <code>Path.DIR_NONE</code> 
     *         if point B is equivalent to point A.
     */
    public static int getIsoDirection (
	IsoSceneModel model, int ax, int ay, int bx, int by)
    {
	if (bx > ax) {
	    if (by == ay) return Path.DIR_SOUTH;
	    return (by < ay) ? Path.DIR_SOUTHEAST : Path.DIR_SOUTHWEST;

	} else if (bx == ax) {
	    if (by == ay) return Path.DIR_NONE;
	    return (by < ay) ? Path.DIR_EAST : Path.DIR_WEST;

	} else {  // bx < ax
	    if (by == ay) return Path.DIR_NORTH;
	    return (by < ay) ? Path.DIR_NORTHEAST : Path.DIR_NORTHWEST;
	}
    }

    /**
     * Convert the given screen-based pixel coordinates to their
     * corresponding tile-based coordinates.  Converted coordinates
     * are placed in the given point object.
     *
     * @param sx the screen x-position pixel coordinate.
     * @param sy the screen y-position pixel coordinate.
     * @param tpos the point object to place coordinates in.
     */
    public static void screenToTile (
	IsoSceneModel model, int sx, int sy, Point tpos)
    {
        Point[] lx = model.lineX, ly = model.lineY;

	// calculate line parallel to the y-axis (from mouse pos to x-axis)
	ly[0].setLocation(sx, sy);
	int bY = (int)(sy - (model.slopeY * sx));

	// determine intersection of x- and y-axis lines
	ly[1].x = (int)((bY - (model.bX + model.origin.y)) /
                        (model.slopeX - model.slopeY));
	ly[1].y = (int)((model.slopeY * ly[1].x) + bY);

	// determine distance of mouse pos along the x axis
	int xdist = (int) MathUtil.distance(
            lx[0].x, lx[0].y, ly[1].x, ly[1].y);
	tpos.x = (int)(xdist / model.tilelen);

	// determine distance of mouse pos along the y-axis
	int ydist = (int) MathUtil.distance(
            ly[0].x, ly[0].y, ly[1].x, ly[1].y);
	tpos.y = (int)(ydist / model.tilelen);
    }

    /**
     * Convert the given tile-based coordinates to their corresponding
     * screen-based pixel coordinates.  Converted coordinates are
     * placed in the given point object.
     *
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     * @param spos the point object to place coordinates in.
     */
    public static void tileToScreen (
	IsoSceneModel model, int x, int y, Point spos)
    {
        spos.x = model.origin.x + ((x - y - 1) * model.tilehwid);
        spos.y = model.origin.y + ((x + y) * model.tilehhei);
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
	IsoSceneModel model, int x, int y, Point fpos)
    {
	// calculate line parallel to the y-axis (from the given
	// x/y-pos to the x-axis)
	float bY = y - (model.fineSlopeY * x);

	// determine intersection of x- and y-axis lines
	int crossx = (int)((bY - model.fineBX) /
			   (model.fineSlopeX - model.fineSlopeY));
	int crossy = (int)((model.fineSlopeY * crossx) + bY);

	// TODO: final position should check distance between our
	// position and the surrounding fine coords and return the
	// actual closest fine coord, rather than just dividing.

	// determine distance along the x-axis
	float xdist = MathUtil.distance(model.tilehwid, 0, crossx, crossy);
	fpos.x = (int)(xdist / model.finelen);

	// determine distance along the y-axis
	float ydist = MathUtil.distance(x, y, crossx, crossy);
	fpos.y = (int)(ydist / model.finelen);
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
     */
    public static void screenToFull (
	IsoSceneModel model, int sx, int sy, Point fpos)
    {
	// get the tile coordinates
	Point tpos = new Point();
	screenToTile(model, sx, sy, tpos);

	// get the screen coordinates for the containing tile
	Point spos = new Point();
	tileToScreen(model, tpos.x, tpos.y, spos);

	// get the fine coordinates within the containing tile
	pixelToFine(model, sx - spos.x, sy - spos.y, fpos);

	// toss in the tile coordinates for good measure
	fpos.x += (tpos.x * FULL_TILE_FACTOR);
	fpos.y += (tpos.y * FULL_TILE_FACTOR);
    }

    /** Multiplication factor to embed tile coords in full coords. */
    protected static final int FULL_TILE_FACTOR = 100;
}
