//
// $Id: IsoUtil.java,v 1.7 2001/10/12 00:42:08 shaper Exp $

package com.threerings.miso.scene.util;

import java.awt.*;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.util.MathUtil;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;

/**
 * The <code>IsoUtil</code> class is a holding place for miscellaneous
 * isometric-display-related utility routines.
 */
public class IsoUtil
{
    public static Polygon getObjectBounds (
        IsoSceneViewModel model, Polygon root, ObjectTile tile)
    {
	Rectangle bounds = root.getBounds();
        int sx = bounds.x - ((tile.baseWidth - 1) * model.tilehwid);
        int sy = bounds.y - tile.height + model.tilehei;

        Polygon boundsPoly = new Polygon();
        int rx = sx, ry = sy;

        // top-left point
        boundsPoly.addPoint(rx, ry);

        // top-right point
        rx = sx + tile.width;
        boundsPoly.addPoint(rx, ry);

        // bottom-right point
        ry = bounds.y - ((tile.baseHeight - 2) * model.tilehhei);
        boundsPoly.addPoint(rx, ry);

        // bottom-middle point
        rx = bounds.x + model.tilehwid;
        ry = bounds.y + model.tilehei;
        boundsPoly.addPoint(rx, ry);

        // bottom-left point
        rx = sx;
        ry = bounds.y - ((tile.baseWidth - 2) * model.tilehhei);
        boundsPoly.addPoint(rx, ry);

        // top-left point
        boundsPoly.addPoint(sx, sy);

        return boundsPoly;
    }

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
	IsoSceneViewModel model, int ax, int ay, int bx, int by)
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
	int dir = getIsoDirection(tax, tay, tbx, tby);
	if (dir != Sprite.DIR_NONE) return dir;

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
	return (dir == -1) ? Sprite.DIR_SOUTHWEST : dir;
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
     * @return the direction specified as one of the <code>Sprite</code>
     *         class's direction constants, or <code>Sprite.DIR_NONE</code> 
     *         if point B is equivalent to point A.
     */
    public static int getIsoDirection (int ax, int ay, int bx, int by)
    {
	if (bx > ax) {
	    if (by == ay) return Sprite.DIR_SOUTH;
	    return (by < ay) ? Sprite.DIR_SOUTHEAST : Sprite.DIR_SOUTHWEST;

	} else if (bx == ax) {
	    if (by == ay) return Sprite.DIR_NONE;
	    return (by < ay) ? Sprite.DIR_EAST : Sprite.DIR_WEST;

	} else {  // bx < ax
	    if (by == ay) return Sprite.DIR_NORTH;
	    return (by < ay) ? Sprite.DIR_NORTHEAST : Sprite.DIR_NORTHWEST;
	}
    }

    public static int fullToTile (int val)
    {
	return (val / FULL_TILE_FACTOR);
    }

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
     */
    public static void screenToTile (
	IsoSceneViewModel model, int sx, int sy, Point tpos)
    {
	// calculate line parallel to the y-axis (from mouse pos to x-axis)
	int bY = (int)(sy - (model.slopeY * sx));

	// determine intersection of x- and y-axis lines
	int crossx = (int)((bY - (model.bX + model.origin.y)) /
			   (model.slopeX - model.slopeY));
	int crossy = (int)((model.slopeY * crossx) + bY);

	// determine distance of mouse pos along the x axis
	int xdist = (int)MathUtil.distance(
	    model.origin.x, model.origin.y, crossx, crossy);
	tpos.x = (int)(xdist / model.tilelen);

	// determine distance of mouse pos along the y-axis
	int ydist = (int)MathUtil.distance(sx, sy, crossx, crossy);
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
	IsoSceneViewModel model, int x, int y, Point spos)
    {
        spos.x = model.origin.x + ((x - y - 1) * model.tilehwid);
        spos.y = model.origin.y + ((x + y) * model.tilehhei);
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
	IsoSceneViewModel model, int x, int y, Point ppos)
    {
	ppos.x = model.tilehwid + ((x - y) * model.finehwid);
	ppos.y = (x + y) * model.finehhei;
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
	IsoSceneViewModel model, int x, int y, Point fpos)
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
	IsoSceneViewModel model, int sx, int sy, Point fpos)
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

    /**
     * Convert the given full coordinates to screen-based pixel
     * coordinates.  Converted coordinates are placed in the given
     * point object.
     *
     * @param x the x-position full coordinate.
     * @param y the y-position full coordinate.
     * @param spos the point object to place coordinates in.
     */
    public static void fullToScreen (
	IsoSceneViewModel model, int x, int y, Point spos)
    {
	// get the tile screen position
	Point tspos = new Point();
	int tx = x / FULL_TILE_FACTOR, ty = y / FULL_TILE_FACTOR;
	tileToScreen(model, tx, ty, tspos);

	// get the pixel position of the fine coords within the tile
	Point ppos = new Point();
	int fx = x - (tx * FULL_TILE_FACTOR), fy = y - (ty * FULL_TILE_FACTOR);
	fineToPixel(model, fx, fy, ppos);

	// final position is tile position offset by fine position
	spos.x = tspos.x + ppos.x;
	spos.y = tspos.y + ppos.y;
    }

    /**
     * Return a polygon framing the specified tile.
     *
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     */
    public static Polygon getTilePolygon (
	IsoSceneViewModel model, int x, int y)
    {
        // get the top-left screen coordinate for the tile
        Point spos = new Point();
        IsoUtil.tileToScreen(model, x, y, spos);

        // create a polygon framing the tile
        Polygon poly = new Polygon();
        poly.addPoint(spos.x, spos.y + model.tilehhei);
        poly.addPoint(spos.x + model.tilehwid, spos.y);
        poly.addPoint(spos.x + model.tilewid, spos.y + model.tilehhei);
        poly.addPoint(spos.x + model.tilehwid, spos.y + model.tilehei);
        poly.addPoint(spos.x, spos.y + model.tilehhei);

        return poly;
    }

    /** Multiplication factor to embed tile coords in full coords. */
    protected static final int FULL_TILE_FACTOR = 100;
}
