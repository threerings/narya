//
// $Id: MisoSceneMetrics.java,v 1.1 2003/04/17 19:21:16 mdb Exp $

package com.threerings.miso.util;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Contains information on the configuration of a particular isometric
 * view. The member data are public to facilitate convenient referencing
 * by the {@link MisoScenePanel} class, the values should not be modified
 * once the metrics are constructed.
 */
public class MisoSceneMetrics
{
    /** Tile dimensions and half-dimensions in the view. */
    public int tilewid, tilehei, tilehwid, tilehhei;

    /** Fine coordinate dimensions. */
    public int finehwid, finehhei;

    /** Number of fine coordinates on each axis within a tile. */
    public int finegran;

    /** Size of the view in tile count. */
    public int scenevwid, scenevhei;

    /** Dimensions of our scene blocks in tile count. */
    public int blockwid = 10, blockhei = 10;

    /** Whether or not this view can extend beyond the bounds defined by
     * the view width and height. True if it cannot, false if it can. */
    public boolean bounded = true;

    /** The bounds of the view in screen pixel coordinates. */
    public Rectangle bounds;

    /** The position in pixels at which tile (0, 0) is drawn. */ 
    public Point origin;

    /** The length of a tile edge in pixels. */
    public float tilelen;

    /** The slope of the x- and y-axis lines. */
    public float slopeX, slopeY;

    /** The length between fine coordinates in pixels. */
    public float finelen;

    /** The y-intercept of the x-axis line within a tile. */
    public float fineBX;

    /** The slope of the x- and y-axis lines within a tile. */
    public float fineSlopeX, fineSlopeY;

    /**
     * Constructs scene metrics by directly specifying the desired config
     * parameters.
     *
     * @param tilewid the width in pixels of the tiles.
     * @param tilehei the height in pixels of the tiles.
     * @param finegran the number of sub-tile divisions to use for fine
     * coordinates.
     * @param svwid the width in tiles of the viewport.
     * @param svhei the height in tiles of the viewport.
     * @param offy the offset of the origin (in tiles) from the top of the
     * viewport.
     */
    public MisoSceneMetrics (int tilewid, int tilehei, int finegran,
                             int svwid, int svhei, int offy)
    {
        // keep track of this stuff
        this.tilewid = tilewid;
        this.tilehei = tilehei;
        this.finegran = finegran;
        this.scenevwid = svwid;
        this.scenevhei = svhei;

	// set the desired scene view bounds
	bounds = new Rectangle(0, 0, scenevwid * tilewid, scenevhei * tilehei);

	// set the scene display origin
        origin = new Point((bounds.width / 2), (offy * tilehei));

        // halve the dimensions
        tilehwid = (tilewid / 2);
        tilehhei = (tilehei / 2);

        // calculate the length of a tile edge in pixels
        tilelen = (float) Math.sqrt(
            (tilehwid * tilehwid) + (tilehhei * tilehhei));

        // calculate the slope of the x- and y-axis lines
        slopeX = (float)tilehei / (float)tilewid;
        slopeY = -slopeX;

	// calculate the edge length separating each fine coordinate
	finelen = tilelen / (float)finegran;

	// calculate the fine-coordinate x-axis line
	fineSlopeX = (float)tilehei / (float)tilewid;
	fineBX = -(fineSlopeX * (float)tilehwid);
	fineSlopeY = -fineSlopeX;

	// calculate the fine coordinate dimensions
	finehwid = (int)((float)tilehwid / (float)finegran);
	finehhei = (int)((float)tilehhei / (float)finegran);
    }

    /**
     * Returns whether the given tile coordinate is a valid coordinate in
     * our coordinate system (which allows tile coordinates from 0 to
     * 2^15-1).
     */
    public boolean isCoordinateValid (int x, int y)
    {
        return (x >= 0 && x < Short.MAX_VALUE &&
                y >= 0 && y < Short.MAX_VALUE);
    }

    /**
     * Returns whether the given full coordinate is a valid coordinate
     * within the scene.
     */
    public boolean isFullCoordinateValid (int x, int y)
    {
        int tx = MisoUtil.fullToTile(x), ty = MisoUtil.fullToTile(y);
        int fx = MisoUtil.fullToFine(x), fy = MisoUtil.fullToFine(y);
        return (isCoordinateValid(tx, ty) &&
                fx >= 0 && fx < finegran &&
                fy >= 0 && fy < finegran);
    }
}
