//
// $Id: IsoSceneView.java,v 1.17 2001/07/28 01:31:51 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.Log;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;
import com.threerings.miso.util.MathUtil;

import java.awt.*;
import java.awt.image.*;

/**
 * The IsoSceneView provides an isometric graphics view of a
 * particular scene.
 */
public class IsoSceneView implements EditableSceneView
{
    /**
     * Construct an IsoSceneView object and initialize it with the
     * given tile manager.
     *
     * @param tmgr the tile manager.
     */
    public IsoSceneView (TileManager tmgr)
    {
	_tmgr = tmgr;

	_bounds = new Dimension(DEF_BOUNDS_WIDTH, DEF_BOUNDS_HEIGHT);

	_htile = new Point();
	_htile.x = _htile.y = -1;

	_font = new Font("Arial", Font.PLAIN, 7);

	_lineX = new Point[2];
	_lineY = new Point[2];
	for (int ii = 0; ii < 2; ii++) {
	    _lineX[ii] = new Point();
	    _lineY[ii] = new Point();
	}

        // pre-calculate the unchanging X-axis line
        calculateXAxis();

	_showCoords = false;
    }

    /**
     * Paint the scene view and any highlighted tiles to the given
     * graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g)
    {
	Graphics2D gfx = (Graphics2D)g;

	// clip the drawing region to our desired bounds since we
	// currently draw tiles willy-nilly in undesirable areas.
  	Shape oldclip = gfx.getClip();
  	gfx.setClip(0, 0, _bounds.width, _bounds.height);

	// draw the full scene into the offscreen image buffer
	renderScene(gfx);

        // draw an outline around the highlighted tile
        paintHighlightedTile(gfx, _htile.x, _htile.y);

        // draw lines illustrating tracking of the mouse position
	paintMouseLines(gfx);

	// restore the original clipping region
	gfx.setClip(oldclip);
    }

    /**
     * Render the scene to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    protected void renderScene (Graphics2D gfx)
    {
	int mx = 1;
	int my = 0;

	int screenY = DEF_CENTER_Y;

	for (int ii = 0; ii < TILE_RENDER_ROWS; ii++) {
	    // determine starting tile coordinates
	    int tx = (ii < Scene.TILE_HEIGHT) ? 0 : mx++;
	    int ty = my;

	    // determine number of tiles in this row
	    int length = (ty - tx) + 1;

	    // determine starting screen x-position
	    int screenX = DEF_CENTER_X - ((length) * ISO_TILE_HALFWIDTH);

	    for (int jj = 0; jj < length; jj++) {

		for (int kk = 0; kk < Scene.NUM_LAYERS; kk++) {
		    // grab the tile we're rendering
		    Tile tile = _scene.tiles[tx][ty][kk];
		    if (tile == null) continue;

		    // determine screen y-position, accounting for
		    // tile image height
		    int ypos = screenY - (tile.height - ISO_TILE_HEIGHT);

		    // draw the tile image at the appropriate screen position
		    gfx.drawImage(tile.img, screenX, ypos, null);
		}

		// draw tile coordinates in each tile
  		if (_showCoords) paintCoords(gfx, tx, ty, screenX, screenY);

		// each tile is one tile-width to the right of the previous
		screenX += ISO_TILE_WIDTH;

		// advance tile x and decrement tile y as we move to
		// the right drawing the row
		tx++;
		ty--;
	    }

	    // each row is a half-tile-height away from the previous row
	    screenY += ISO_TILE_HALFHEIGHT;

	    // advance starting y-axis coordinate unless we've hit bottom
	    if ((++my) > Scene.TILE_HEIGHT - 1) my = Scene.TILE_HEIGHT - 1;
	}
    }

    /**
     * Paint lines showing the most recently calculated x- and y-axis
     * mouse position tracking lines, and the mouse position itself.
     *
     * @param gfx the graphics context.
     */
    protected void paintMouseLines (Graphics2D gfx)
    {
	// draw the baseline x-axis line
	gfx.setColor(Color.red);
	gfx.drawLine(_lineX[0].x, _lineX[0].y, _lineX[1].x, _lineX[1].y);

	// draw line from last mouse pos to baseline
	gfx.setColor(Color.yellow);
	gfx.drawLine(_lineY[0].x, _lineY[0].y, _lineY[1].x, _lineY[1].y);

	// draw the most recent mouse cursor position
	gfx.setColor(Color.green);
	gfx.fillRect(_lineY[0].x, _lineY[0].y, 2, 2);
	gfx.setColor(Color.red);
	gfx.drawRect(_lineY[0].x - 1, _lineY[0].y - 1, 3, 3);
    }

    /**
     * Paint the tile coordinate numbers in tile (x, y) whose top-left
     * corner is at screen pixel coordinates (sx, sy).
     *
     * @param gfx the graphics context.
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     * @param sx the screen x-position pixel coordinate.
     * @param sy the screen y-position pixel coordinate.
     */
    protected void paintCoords (Graphics2D gfx, int x, int y, int sx, int sy)
    {
	gfx.setFont(_font);
	gfx.setColor(Color.white);
	gfx.drawString("" + x, sx + ISO_TILE_HALFWIDTH - 2,
                       sy + ISO_TILE_HALFHEIGHT - 2);
	gfx.drawString("" + y, sx + ISO_TILE_HALFWIDTH - 2,
                       sy + ISO_TILE_HEIGHT - 2);
    }

    /**
     * Paint a highlight around the tile at screen pixel coordinates
     * (sx, sy).
     *
     * @param gfx the graphics context.
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     */
    protected void paintHighlightedTile (Graphics2D gfx, int x, int y)
    {
        Point spos = new Point();
        tileToScreen(x, y, spos);

        // set the desired stroke and color
	Stroke ostroke = gfx.getStroke();
	gfx.setStroke(HLT_STROKE);
	gfx.setColor(HLT_COLOR);

        // draw the tile outline
	gfx.drawLine(spos.x, spos.y + ISO_TILE_HALFHEIGHT,
		     spos.x + ISO_TILE_HALFWIDTH, spos.y);
	gfx.drawLine(spos.x + ISO_TILE_HALFWIDTH, spos.y,
		     spos.x + ISO_TILE_WIDTH, spos.y + ISO_TILE_HALFHEIGHT);
	gfx.drawLine(spos.x + ISO_TILE_WIDTH, spos.y + ISO_TILE_HALFHEIGHT,
		     spos.x + ISO_TILE_HALFWIDTH, spos.y + ISO_TILE_HEIGHT);
	gfx.drawLine(spos.x + ISO_TILE_HALFWIDTH, spos.y + ISO_TILE_HEIGHT,
		     spos.x, spos.y + ISO_TILE_HALFHEIGHT);

        // restore the original stroke
	gfx.setStroke(ostroke);
    }

    /**
     * Highlight the tile at the specified pixel coordinates the next
     * time the scene is re-rendered.
     *
     * @param sx the screen x-position pixel coordinate.
     * @param sy the screen y-position pixel coordinate.
     */
    public void setHighlightedTile (int sx, int sy)
    {
        screenToTile(sx, sy, _htile);
    }

    /**
     * Pre-calculate the x-axis line (from tile origin to right end of
     * x-axis) for later use in converting tile and screen
     * coordinates.
     */
    protected void calculateXAxis ()
    {
        // determine the starting point
	_lineX[0].x = DEF_CENTER_X;
	_bX = (int)-(SLOPE_X * _lineX[0].x);
	_lineX[0].y = DEF_CENTER_Y;

        // determine the ending point
	_lineX[1].x = _lineX[0].x + (ISO_TILE_HALFWIDTH * Scene.TILE_WIDTH);
	_lineX[1].y = _lineX[0].y + (int)((SLOPE_X * _lineX[1].x) + _bX);
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
    protected void screenToTile (int sx, int sy, Point tpos)
    {
	// calculate line parallel to the y-axis (from mouse pos to x-axis)
	_lineY[0].x = sx;
	_lineY[0].y = sy;
	int bY = (int)(sy - (SLOPE_Y * sx));

	// determine intersection of x- and y-axis lines
	_lineY[1].x = (int)((bY - (_bX + DEF_CENTER_Y)) / (SLOPE_X - SLOPE_Y));
	_lineY[1].y = (int)((SLOPE_Y * _lineY[1].x) + bY);

	// determine distance of mouse pos along the x axis
	int xdist = (int) MathUtil.distance(
            _lineX[0].x, _lineX[0].y, _lineY[1].x, _lineY[1].y);
	tpos.x = (int)(xdist / TILE_EDGE_LENGTH);

	// determine distance of mouse pos along the y-axis
	int ydist = (int) MathUtil.distance(
            _lineY[0].x, _lineY[0].y, _lineY[1].x, _lineY[1].y);
	tpos.y = (int)(ydist / TILE_EDGE_LENGTH);
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
    protected void tileToScreen (int x, int y, Point spos)
    {
        spos.x = _lineX[0].x + ((x - y - 1) * ISO_TILE_HALFWIDTH);
        spos.y = _lineX[0].y + ((x + y) * ISO_TILE_HALFHEIGHT);
    }

    public void setScene (Scene scene)
    {
	_scene = scene;
    }

    public void setShowCoordinates (boolean show)
    {
	_showCoords = show;
    }

    public void setTile (int x, int y, int lnum, Tile tile)
    {
	Point tpos = new Point();
        screenToTile(x, y, tpos);
	_scene.tiles[tpos.x][tpos.y][lnum] = tile;
    }

    protected static final int ISO_TILE_HEIGHT = 16;
    protected static final int ISO_TILE_WIDTH = 32;

    protected static final int ISO_TILE_HALFHEIGHT = ISO_TILE_HEIGHT / 2;
    protected static final int ISO_TILE_HALFWIDTH = ISO_TILE_WIDTH / 2;

    /** The default width of a scene in pixels. */
    protected static final int DEF_BOUNDS_WIDTH = 18 * ISO_TILE_WIDTH;

    /** The default height of a scene in pixels. */
    protected static final int DEF_BOUNDS_HEIGHT = 37 * ISO_TILE_HEIGHT;

    /** The total number of tile rows to render the full scene view. */
    protected static final int TILE_RENDER_ROWS =
        (Scene.TILE_WIDTH * Scene.TILE_HEIGHT) - 1;

    /** The starting x-position to render the view. */
    protected static final int DEF_CENTER_X = DEF_BOUNDS_WIDTH / 2;

    /** The starting y-position to render the view. */
    protected static final int DEF_CENTER_Y = -(9 * ISO_TILE_HEIGHT);

    /** The length of a tile edge in pixels from an isometric perspective. */
    protected static final float TILE_EDGE_LENGTH = (float)
        Math.sqrt((ISO_TILE_HALFWIDTH * ISO_TILE_HALFWIDTH) +
                  (ISO_TILE_HALFHEIGHT * ISO_TILE_HALFHEIGHT));

    /** The color to draw the highlighted tile. */
    protected static final Color HLT_COLOR = Color.green;

    /** The stroke object used to draw the highlighted tile. */
    protected static final Stroke HLT_STROKE = new BasicStroke(3);

    /** The slope of the x-axis line. */
    protected float SLOPE_X = 0.5f;

    /** The slope of the y-axis line. */
    protected float SLOPE_Y = -0.5f;

    /** The y-intercept of the x-axis line. */
    protected int _bX;

    /** The last calculated x- and y-axis mouse position tracking lines. */
    protected Point _lineX[], _lineY[];

    /** The bounds dimensions for the view. */
    protected Dimension _bounds;

    /** The currently highlighted tile. */
    protected Point _htile;

    /** The font to draw tile coordinates. */
    protected Font _font;

    /** Whether tile coordinates should be drawn. */
    protected boolean _showCoords;

    /** The scene object to be displayed. */
    protected Scene _scene;

    /** The tile manager. */
    protected TileManager _tmgr;
}
