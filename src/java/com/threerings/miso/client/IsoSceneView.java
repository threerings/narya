//
// $Id: IsoSceneView.java,v 1.8 2001/07/19 00:22:02 shaper Exp $

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
	
	_viewX = 0;
	_viewY = 0;

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
    }

    public void paint (Graphics g)
    {
	Graphics2D gfx = (Graphics2D)g;

	gfx.setColor(Color.red);
	gfx.fillRect(0, 0, _bounds.width, _bounds.height);

	// draw the full scene into the offscreen image buffer
	renderScene(gfx, _viewX, _viewY);
    }

    protected void renderScene (Graphics2D g2, int x, int y)
    {
	int mapX = x / Tile.HALF_WIDTH;
	int xOff = x & (Tile.HALF_WIDTH - 1);

	int mapY = y / Tile.HEIGHT;
	int yOff = y & Tile.HEIGHT - 1;

	int xa   = xOff - yOff;
	int ya   = (xOff >> 1) + (yOff >> 1);

	int mx = mapX;
	int my = mapY;

	int screenY = OFF_Y + (Tile.HALF_HEIGHT - ya);

	for (int ii = 0; ii < Scene.TILE_HEIGHT; ii++) {
	    int tx = mx;
	    int ty = my;

	    int screenX = OFF_X + (Tile.HALF_WIDTH - xa);

	    int length = Scene.TILE_WIDTH;
	    if ((ii & 1) == 1) {
		length++;
		screenX -= Tile.HALF_WIDTH;
	    }

	    for (int j = 0; j < length; j++) {
		// TODO: draw layers L1+.
		Tile tile = _scene.tiles[tx][ty][Scene.LAYER_BASE];

		int ypos = screenY - (tile.height - Tile.HEIGHT);
		g2.drawImage(tile.img, screenX, ypos, null);

		//paintCoords(g2, tx, ty, screenX, screenY);

		// draw an outline around the highlighted tile
		if (tx == _htile.x && ty == _htile.y) {
		    paintHighlightedTile(g2, screenX, screenY);
		}

		screenX += Tile.WIDTH;

		if ((tx += 1) > Scene.TILE_WIDTH - 1) tx = 0;
		if ((ty -= 1) < 0) ty = Scene.TILE_HEIGHT - 1;
	    }

	    screenY += Tile.HALF_HEIGHT;

	    if ((ii & 1) == 1) {
		if ((mx += 1) > Scene.TILE_WIDTH - 1) mx = 0;
	    } else {
		if ((my += 1) > Scene.TILE_HEIGHT - 1) my = 0;
	    }
	}

	// draw the baseline x-axis line
	g2.setColor(Color.red);
	g2.drawLine(_lineX[0].x, _lineX[0].y, _lineX[1].x, _lineX[1].y);

	// draw line from last mouse pos to baseline
	g2.setColor(Color.yellow);
	g2.drawLine(_lineY[0].x, _lineY[0].y, _lineY[1].x, _lineY[1].y);

	// draw the most recent mouse cursor position
	g2.setColor(Color.green);
	g2.fillRect(_lineY[0].x, _lineY[0].y, 2, 2);
	g2.setColor(Color.red);
	g2.drawRect(_lineY[0].x - 1, _lineY[0].y - 1, 3, 3);
    }

    /**
     * Paint the tile coordinates in tile (x, y) whose top-left corner
     * is at screen coordinates (sx, sy).
     */
    protected void paintCoords (Graphics2D g2, int x, int y, int sx, int sy)
    {
	g2.setFont(_font);
	g2.setColor(Color.white);
	g2.drawString(""+x, sx+Tile.HALF_WIDTH-2, sy+Tile.HALF_HEIGHT-2);
	g2.drawString(""+y, sx+Tile.HALF_WIDTH-2, sy+Tile.HEIGHT-2);
    }

    /**
     * Paint a highlight around the tile at screen coordinates (sx, sy).
     */
    protected void paintHighlightedTile (Graphics2D g2, int sx, int sy)
    {
	int x = sx;
	int y = sy;

	Stroke ostroke = g2.getStroke();
	g2.setStroke(_hstroke);
	g2.setColor(_hcolor);

	g2.drawLine(x, y + Tile.HALF_HEIGHT,
		    x + Tile.HALF_WIDTH, y);
	g2.drawLine(x + Tile.HALF_WIDTH, y,
		    x + Tile.WIDTH, y + Tile.HALF_HEIGHT);
	g2.drawLine(x + Tile.WIDTH, y + Tile.HALF_HEIGHT,
		    x + Tile.HALF_WIDTH, y + Tile.HEIGHT);
	g2.drawLine(x + Tile.HALF_WIDTH, y + Tile.HEIGHT,
		    x, y + Tile.HALF_HEIGHT);

	g2.setStroke(ostroke);
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
	_lineX[0].x = Tile.HALF_WIDTH;
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

    public void setTile (int x, int y, int lnum, Tile tile)
    {
	Point tpos = screenToTile(x, y);
	_scene.tiles[tpos.x][tpos.y][lnum] = tile;
    }

    protected static final int OFF_X = 0;
    protected static final int OFF_Y = 0;

    protected static final int DEF_BOUNDS_WIDTH = 600;
    protected static final int DEF_BOUNDS_HEIGHT = 600;

    protected Point _lineX[], _lineY[];

    protected Rectangle _bounds;
    protected int _viewX, _viewY;

    protected Point _htile;
    protected Color _hcolor;
    protected Stroke _hstroke;

    protected Font _font;

    protected Scene _scene;
    protected TileManager _tmgr;
}
