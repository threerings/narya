//
// $Id: IsoSceneView.java,v 1.11 2001/07/20 03:50:35 shaper Exp $

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
public class IsoSceneView implements SceneView
{
    public IsoSceneView (TileManager tmgr)
    {
	_tmgr = tmgr;

	_bounds = new Rectangle(0, 0, DEF_BOUNDS_WIDTH, DEF_BOUNDS_HEIGHT);

	_htile = new Point();
	_htile.x = _htile.y = -1;

	_hcolor = Color.green;
	_hstroke = new BasicStroke(3);

	_font = new Font("Arial", Font.PLAIN, 7);

	_lineX = new Point[2];
	_lineY = new Point[2];
	for (int ii = 0; ii < 2; ii++) {
	    _lineX[ii] = new Point();
	    _lineY[ii] = new Point();
	}

	_showCoords = false;
    }

    public void paint (Graphics g)
    {
	Graphics2D gfx = (Graphics2D)g;

//  	gfx.setColor(Color.red);
//  	gfx.fillRect(0, 0, _bounds.width, _bounds.height);

	// draw the full scene into the offscreen image buffer
	renderScene(gfx);
    }

    protected void renderScene (Graphics2D gfx)
    {
	int mx = 1;
	int my = 0;

	int screenY = Tile.HALF_HEIGHT;

	for (int ii = 0; ii < TILE_RENDER_ROWS; ii++) {
	    // determine starting tile coordinates
	    int tx = (ii < Scene.TILE_HEIGHT) ? 0 : mx++;
	    int ty = my;

	    // determine number of tiles in this row
	    int length = (ty - tx) + 1;

	    // determine starting screen x-position
	    int screenX = DEF_CENTER_X - ((length - 1) * Tile.HALF_WIDTH);

	    for (int jj = 0; jj < length; jj++) {

		for (int kk = 0; kk < Scene.NUM_LAYERS; kk++) {
		    // grab the tile we're rendering
		    Tile tile = _scene.tiles[tx][ty][kk];
		    if (tile == null) continue;

		    // determine screen y-position, accounting for
		    // tile image height
		    int ypos = screenY - (tile.height - Tile.HEIGHT);

		    // draw the tile image at the appropriate screen position
		    gfx.drawImage(tile.img, screenX, ypos, null);
		}

		// draw tile coordinates in each tile
  		if (_showCoords) paintCoords(gfx, tx, ty, screenX, screenY);

		// draw an outline around the highlighted tile
		if (tx == _htile.x && ty == _htile.y) {
		    paintHighlightedTile(gfx, screenX, screenY);
		}

		// each tile is one tile-width to the right of the previous
		screenX += Tile.WIDTH;

		// advance tile x and decrement tile y as we move to
		// the right drawing the row
		tx++;
		ty--;
	    }

	    // each row is a half-tile-height away from the previous row
	    screenY += Tile.HALF_HEIGHT;

	    // advance starting y-axis coordinate unless we've hit bottom
	    if ((++my) > Scene.TILE_HEIGHT - 1) my = Scene.TILE_HEIGHT - 1;
	}

	paintMouseLines(gfx);
    }

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
     * Paint the tile coordinates in tile (x, y) whose top-left corner
     * is at screen coordinates (sx, sy).
     */
    protected void paintCoords (Graphics2D gfx, int x, int y, int sx, int sy)
    {
	gfx.setFont(_font);
	gfx.setColor(Color.white);
	gfx.drawString(""+x, sx+Tile.HALF_WIDTH-2, sy+Tile.HALF_HEIGHT-2);
	gfx.drawString(""+y, sx+Tile.HALF_WIDTH-2, sy+Tile.HEIGHT-2);
    }

    /**
     * Paint a highlight around the tile at screen coordinates (sx, sy).
     */
    protected void paintHighlightedTile (Graphics2D gfx, int sx, int sy)
    {
	int x = sx;
	int y = sy;

	Stroke ostroke = gfx.getStroke();
	gfx.setStroke(_hstroke);
	gfx.setColor(_hcolor);

	gfx.drawLine(x, y + Tile.HALF_HEIGHT,
		     x + Tile.HALF_WIDTH, y);
	gfx.drawLine(x + Tile.HALF_WIDTH, y,
		     x + Tile.WIDTH, y + Tile.HALF_HEIGHT);
	gfx.drawLine(x + Tile.WIDTH, y + Tile.HALF_HEIGHT,
		     x + Tile.HALF_WIDTH, y + Tile.HEIGHT);
	gfx.drawLine(x + Tile.HALF_WIDTH, y + Tile.HEIGHT,
		     x, y + Tile.HALF_HEIGHT);

	gfx.setStroke(ostroke);
    }

    /**
     * Highlight the tile at the specified pixel coordinates the next
     * time the scene is re-rendered.
     */
    public void setHighlightedTile (int x, int y)
    {
	Point tpos = screenToTile(x, y);
	if (tpos != null) _htile = tpos;
//  	Log.info("Highlighting tile [x="+_htile.x+", y="+_htile.y+"].");
    }

    /**
     * Returns a new Point object containing the tile coordinates
     * corresponding to the specified screen-based mouse-position
     * coordinates.
     */
    protected Point screenToTile (int x, int y)
    {
	Point tpos = new Point();

        float mX, mY;
	int bX, bY;

	// calculate the x-axis line (from tile origin to end of visible axis)
	mX = 0.5f;
	_lineX[0].x = DEF_CENTER_X;
	bX = (int)-(mX * _lineX[0].x);
	_lineX[0].y = 0;
	_lineX[1].x = _bounds.width;
	_lineX[1].y = (int)((mX * _lineX[1].x) + bX);

	// calculate line parallel to the y-axis (from mouse pos to x-axis)
	_lineY[0].x = x;
	_lineY[0].y = y;
	mY = -0.5f;
	bY = (int)(_lineY[0].y - (mY * _lineY[0].x));

	// determine intersection of x- and y-axis lines
	_lineY[1].x = (int)((bY - bX) / (mX - mY));
	_lineY[1].y = (int)((mY * _lineY[1].x) + bY);

	// determine distance of mouse pos along the x axis
	int xdist = (int)
	    MathUtil.distance(_lineX[0].x, _lineX[0].y,
			      _lineY[1].x, _lineY[1].y);
	tpos.x = (int)((xdist - Tile.EDGE_LENGTH) / Tile.EDGE_LENGTH);

	// determine distance of mouse pos along the y-axis
	int ydist = (int)
	    MathUtil.distance(_lineY[0].x, _lineY[0].y,
			      _lineY[1].x, _lineY[1].y);
	tpos.y = (int)(ydist / Tile.EDGE_LENGTH);

	//Log.info("[mX="+mX+", bX="+bX+", mY="+mY+", bY="+bY+"]");
	//Log.info("x-axis=" + MathUtil.lineToString(_lineX[0], _lineX[1]));
	//Log.info("y-axis=" + MathUtil.lineToString(_lineY[0], _lineY[1]));

	return tpos;
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
	Point tpos = screenToTile(x, y);
	_scene.tiles[tpos.x][tpos.y][lnum] = tile;
    }

    // default dimensions of the scene view
    protected static final int DEF_BOUNDS_WIDTH = 600;
    protected static final int DEF_BOUNDS_HEIGHT = 600;

    // total number of tile rows to render the full view
    protected static final int TILE_RENDER_ROWS =
        (Scene.TILE_WIDTH * Scene.TILE_HEIGHT) - 1;

    // starting x/y-positions to render the view
    protected static final int DEF_CENTER_X = DEF_BOUNDS_WIDTH / 2;

    protected Point _lineX[], _lineY[];

    protected Rectangle _bounds;

    protected Point _htile;
    protected Color _hcolor;
    protected Stroke _hstroke;

    protected Font _font;

    protected boolean _showCoords;

    protected Scene _scene;
    protected TileManager _tmgr;
}
