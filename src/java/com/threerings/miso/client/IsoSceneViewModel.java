//
// $Id: IsoSceneViewModel.java,v 1.1 2001/08/02 00:42:02 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Dimension;
import java.awt.Point;

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

    /** The last calculated x- and y-axis mouse position tracking lines. */
    public Point lineX[], lineY[];

    /** Whether tile coordinates should be drawn. */
    public boolean showCoords;

    /**
     * Construct an IsoSceneModel with reasonable default values.
     */
    public IsoSceneModel ()
    {
        setTileDimensions(32, 16);
        setBounds(600, 600);
        setOrigin(bounds.width / 2, -(9 * tilehei));
        showCoords = false;
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
     * Pre-calculate the x-axis line (from tile origin to right end of
     * x-axis) for later use in converting screen coordinates to tile
     * coordinates.
     */
    public void calculateXAxis ()
    {
        // create the x- and y-axis lines
	lineX = new Point[2];
	lineY = new Point[2];
	for (int ii = 0; ii < 2; ii++) {
	    lineX[ii] = new Point();
	    lineY[ii] = new Point();
	}

        // determine the starting point
        lineX[0].setLocation(origin.x, origin.y);
	bX = (int)-(SLOPE_X * origin.x);

        // determine the ending point
	lineX[1].x = lineX[0].x + (tilehwid * Scene.TILE_WIDTH);
	lineX[1].y = lineX[0].y + (int)((SLOPE_X * lineX[1].x) + bX);
    }

    /** The slope of the x-axis line. */
    protected final float SLOPE_X = 0.5f;

    /** The slope of the y-axis line. */
    protected final float SLOPE_Y = -0.5f;
}
