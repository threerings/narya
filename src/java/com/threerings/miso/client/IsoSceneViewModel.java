//
// $Id: IsoSceneViewModel.java,v 1.6 2001/08/10 21:17:07 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Dimension;
import java.awt.Point;

import com.threerings.miso.Log;

/**
 * The IsoSceneModel provides a holding place for the myriad
 * parameters and bits of data that describe the details of an
 * isometric view of a scene.
 *
 * <p> The member data are public to facilitate speedy referencing by
 * the <code>IsoSceneView</code> class.  Those wishing to set up an
 * IsoSceneModel object should do so solely via the constructor and
 * accessor methods.
 */
public class IsoSceneModel
{
    /** Tile dimensions and half-dimensions in the view. */
    public int tilewid, tilehei, tilehwid, tilehhei;

    /** Fine coordinate dimensions. */
    public int finehwid, finehhei;

    /** Number of fine coordinates on each axis within a tile. */
    public int finegran;

    /** The bounds dimensions for the view. */
    public Dimension bounds;

    /** The position in pixels at which tile (0, 0) is drawn. */ 
    public Point origin;

    /** The total number of tile rows to render the full view. */
    public int tilerows;

    /** The length of a tile edge in pixels. */
    public float tilelen;

    /** The y-intercept of the x-axis line. */
    public int bX;

    /** The slope of the x- and y-axis lines. */
    public float slopeX, slopeY;

    /** The x-axis line. */
    public Point lineX[];

    /** The length between fine coordinates in pixels. */
    public float finelen;

    /** The y-intercept of the x-axis line within a tile. */
    public float fineBX;

    /** The slope of the x- and y-axis lines within a tile. */
    public float fineSlopeX, fineSlopeY;

    /** Whether tile coordinates should be drawn. */
    public boolean showCoords;

    /** Whether locations in the scene should be drawn. */
    public boolean showLocs;

    /**
     * Construct an IsoSceneModel with reasonable default values.
     */
    public IsoSceneModel ()
    {
        setTileDimensions(32, 16);
	setFineGranularity(4);
        setBounds(600, 600);
        setOrigin(bounds.width / 2, -(9 * tilehei));
        showCoords = false;
	showLocs = false;
    }

    /**
     * Set the number of fine coordinates within a tile.  The
     * granularity determines the number of coordinates on both the x-
     * and y-axis.
     *
     * @param gran the number of fine coordinates on each axis.
     */
    public void setFineGranularity (int gran)
    {
	finegran = gran;
    }

    /**
     * Return whether locations in the scene are currently drawn.
     */
    public boolean getShowLocations ()
    {
	return showLocs;
    }

    /**
     * Set whether locations in the scene should be drawn.
     *
     * @param show whether to show locations.
     */
    public void setShowLocations (boolean show)
    {
	showLocs = show;
    }

    /**
     * Return whether coordinates are currently drawn for each tile.
     */
    public boolean getShowCoordinates ()
    {
	return showCoords;
    }

    /**
     * Set whether coordinates should be drawn for each tile.
     *
     * @param show whether to show coordinates.
     */
    public void setShowCoordinates (boolean show)
    {
	showCoords = show;
    }

    /**
     * Set the dimensions of the tiles that comprise the base layer of
     * the isometric view and therefore drive the view geometry as a
     * whole.
     *
     * @param width the tile width in pixels.
     * @param height the tile height in pixels.
     */
    public void setTileDimensions (int width, int height)
    {
        // save the dimensions
        tilewid = width;
        tilehei = height;

        // halve the dimensions
        tilehwid = width / 2;
        tilehhei = height / 2;

        // calculate the length of a tile edge in pixels
        tilelen = (float) Math.sqrt(
            (tilehwid * tilehwid) + (tilehhei * tilehhei));

        // calculate the number of tile rows to render
        tilerows = (Scene.TILE_WIDTH * Scene.TILE_HEIGHT) - 1;

        // calculate the slope of the x- and y-axis lines
        slopeX = (float)tilehei / (float)tilewid;
        slopeY = -slopeX;
    }

    /**
     * Set the origin position at which the isometric view is
     * displayed in screen pixel coordinates.
     *
     * @param x the x-position in pixels.
     * @param y the y-position in pixels.
     */
    public void setOrigin (int x, int y)
    {
        // save the requested origin
        origin = new Point(x, y);
    }

    /**
     * Set the desired bounds for the view.  The actual resulting
     * bounds will be based on the number of whole tiles that fit in
     * the requested bounds vertically and horizontally.  For the
     * bounds to be calculated correctly, therefore, the desired tile
     * dimensions should have been previously set via
     * <code>setTileDimensions()</code>.
     *
     * @param width the bounds width in pixels.
     * @param height the bounds height in pixels.
     */
    public void setBounds (int width, int height)
    {
        // determine the actual bounds based on the number of whole
        // tiles that fit in the requested bounds
        int bwid = (width / tilewid) * tilewid;
        int bhei = (height / tilehei) * tilehei;

        // save our calculated boundaries in pixel coordinates
        bounds = new Dimension(bwid, bhei);
    }

    /**
     * Pre-calculate various values that are commonly used in working
     * with an isometric view.  Includes x-axis line values for use in
     * converting coordinates, and fine coordinate dimensions.
     */
    public void precalculate ()
    {
	// calculate scene-based x-axis line for conversion from
	// screen to tile coordinates

        // create the x- and y-axis lines
	lineX = new Point[2];
	for (int ii = 0; ii < 2; ii++) {
	    lineX[ii] = new Point();
	}

        // determine the starting point
        lineX[0].setLocation(origin.x, origin.y);
	bX = (int)-(slopeX * origin.x);

        // determine the ending point
	lineX[1].x = lineX[0].x + (tilehwid * Scene.TILE_WIDTH);
	lineX[1].y = lineX[0].y + (int)((slopeX * lineX[1].x) + bX);

	// calculate tile-based x-axis line for conversion from
	// tile-based pixel to fine coordinates

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
}
