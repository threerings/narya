//
// $Id: IsoSceneView.java,v 1.20 2001/08/02 00:42:02 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.Log;
import com.threerings.miso.sprite.SpriteManager;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;
import com.threerings.miso.util.MathUtil;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

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
     * @param tilemgr the tile manager.
     */
    public IsoSceneView (TileManager tilemgr, SpriteManager spritemgr,
                         IsoSceneModel model)
    {
	_tilemgr = tilemgr;
        _spritemgr = spritemgr;

        setModel(model);

        // initialize the highlighted tile
	_htile = new Point(-1, -1);

        // get the font used to render tile coordinates
	_font = new Font("Arial", Font.PLAIN, 7);

        // create the list of dirty rectangles
        _dirty = new ArrayList();
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
  	gfx.setClip(0, 0, _model.bounds.width, _model.bounds.height);

	// draw the full scene into the offscreen image buffer
	//renderSceneInvalid(gfx);
        renderScene(gfx);

        // draw an outline around the highlighted tile
        paintHighlightedTile(gfx, _htile.x, _htile.y);

        // draw lines illustrating tracking of the mouse position
  	//paintMouseLines(gfx);

	// restore the original clipping region
	gfx.setClip(oldclip);
    }

    /**
     * Render the scene to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    protected void renderSceneInvalid (Graphics2D gfx)
    {
        Point spos = new Point();

        Log.info("renderSceneInvalid.");

        int size = _dirty.size();
        for (int ii = 0; ii < size; ii++) {
            int[] dinfo = (int[])_dirty.remove(0);

            tileToScreen(dinfo[0], dinfo[1], spos);

            Log.info("renderSceneInvalid [tx=" + dinfo[0] +
                     ", ty=" + dinfo[1] + ", x=" + spos.x +
                     ", y=" + spos.y + "].");

            Tile tile = _scene.tiles[dinfo[0]][dinfo[1]][0];
            if (tile == null) continue;

            int ypos = spos.y - (tile.height - _model.tilehei);
            gfx.drawImage(tile.img, spos.x, ypos, null);

            // draw all sprites residing in the current tile
            _spritemgr.renderSprites(
                gfx, spos.x, spos.y, _model.tilewid, _model.tilehei);
        }
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

	int screenY = _model.origin.y;

	for (int ii = 0; ii < _model.tilerows; ii++) {
	    // determine starting tile coordinates
	    int tx = (ii < Scene.TILE_HEIGHT) ? 0 : mx++;
	    int ty = my;

	    // determine number of tiles in this row
	    int length = (ty - tx) + 1;

	    // determine starting screen x-position
	    int screenX = _model.origin.x - ((length) * _model.tilehwid);

	    for (int jj = 0; jj < length; jj++) {

		for (int kk = 0; kk < Scene.NUM_LAYERS; kk++) {
		    // grab the tile we're rendering
		    Tile tile = _scene.tiles[tx][ty][kk];
		    if (tile == null) continue;

		    // determine screen y-position, accounting for
		    // tile image height
		    int ypos = screenY - (tile.height - _model.tilehei);

		    // draw the tile image at the appropriate screen position
		    gfx.drawImage(tile.img, screenX, ypos, null);
                }

                // draw all sprites residing in the current line of tiles
                _spritemgr.renderSprites(
                    gfx, screenX, screenY, (length * _model.tilewid),
                    _model.tilehei);

		// draw tile coordinates in each tile
  		if (_model.showCoords) {
                    paintCoords(gfx, tx, ty, screenX, screenY);
                }

		// each tile is one tile-width to the right of the previous
		screenX += _model.tilewid;

		// advance tile x and decrement tile y as we move to
		// the right drawing the row
		tx++;
		ty--;
	    }

	    // each row is a half-tile-height away from the previous row
	    screenY += _model.tilehhei;

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
        Point[] lx = _model.lineX, ly = _model.lineY;
        
	// draw the baseline x-axis line
	gfx.setColor(Color.red);
	gfx.drawLine(lx[0].x, lx[0].y, lx[1].x, lx[1].y);

	// draw line from last mouse pos to baseline
	gfx.setColor(Color.yellow);
	gfx.drawLine(ly[0].x, ly[0].y, ly[1].x, ly[1].y);

	// draw the most recent mouse cursor position
	gfx.setColor(Color.green);
	gfx.fillRect(ly[0].x, ly[0].y, 2, 2);
	gfx.setColor(Color.red);
	gfx.drawRect(ly[0].x - 1, ly[0].y - 1, 3, 3);
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
	gfx.drawString("" + x, sx + _model.tilehwid - 2,
                       sy + _model.tilehhei - 2);
	gfx.drawString("" + y, sx + _model.tilehwid - 2,
                       sy + _model.tilehei - 2);
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
	gfx.drawLine(spos.x, spos.y + _model.tilehhei,
		     spos.x + _model.tilehwid, spos.y);
	gfx.drawLine(spos.x + _model.tilehwid, spos.y,
		     spos.x + _model.tilewid, spos.y + _model.tilehhei);
	gfx.drawLine(spos.x + _model.tilewid, spos.y + _model.tilehhei,
		     spos.x + _model.tilehwid, spos.y + _model.tilehei);
	gfx.drawLine(spos.x + _model.tilehwid, spos.y + _model.tilehei,
		     spos.x, spos.y + _model.tilehhei);

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
     * Invalidate a list of rectangles in the view for later repainting.
     *
     * @param rects the list of Rectangle objects.
     */
    public void invalidateRects (ArrayList rects)
    {
        int size = rects.size();
        for (int ii = 0; ii < size; ii++) {
            Rectangle r = (Rectangle)rects.get(ii);
            invalidateScreenRect(r.x, r.y, r.width, r.height);
        }
    }

    /**
     * Invalidate the specified rectangle in screen pixel coordinates
     * in the view.
     *
     * @param x the rectangle x-position.
     * @param y the rectangle y-position.
     * @param width the rectangle width.
     * @param height the rectangle height.
     */
    public void invalidateScreenRect (int x, int y, int width, int height)
    {
        Point tpos = new Point();
        screenToTile(x, y, tpos);

//          Log.info("invalidateScreenRect: mapped rect to tile " +
//                   "[tx=" + tpos.x + ", ty=" + tpos.y +
//                   ", x=" + x + ", y=" + y + ", width=" + width +
//                   ", height=" + height + "].");

        _dirty.add(new int[] { tpos.x, tpos.y });
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
        Point[] lx = _model.lineX, ly = _model.lineY;

	// calculate line parallel to the y-axis (from mouse pos to x-axis)
	ly[0].setLocation(sx, sy);
	int bY = (int)(sy - (_model.SLOPE_Y * sx));

	// determine intersection of x- and y-axis lines
	ly[1].x = (int)((bY - (_model.bX + _model.origin.y)) /
                        (_model.SLOPE_X - _model.SLOPE_Y));
	ly[1].y = (int)((_model.SLOPE_Y * ly[1].x) + bY);

	// determine distance of mouse pos along the x axis
	int xdist = (int) MathUtil.distance(
            lx[0].x, lx[0].y, ly[1].x, ly[1].y);
	tpos.x = (int)(xdist / _model.tilelen);

	// determine distance of mouse pos along the y-axis
	int ydist = (int) MathUtil.distance(
            ly[0].x, ly[0].y, ly[1].x, ly[1].y);
	tpos.y = (int)(ydist / _model.tilelen);
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
        spos.x = _model.lineX[0].x + ((x - y - 1) * _model.tilehwid);
        spos.y = _model.lineX[0].y + ((x + y) * _model.tilehhei);
    }

    public void setScene (Scene scene)
    {
	_scene = scene;
    }

    public void setShowCoordinates (boolean show)
    {
	_model.showCoords = show;
    }

    public void setTile (int x, int y, int lnum, Tile tile)
    {
	Point tpos = new Point();
        screenToTile(x, y, tpos);
	_scene.tiles[tpos.x][tpos.y][lnum] = tile;
    }

    public void setModel (IsoSceneModel model)
    {
        _model = model;
        _model.calculateXAxis();
    }

    /** The color to draw the highlighted tile. */
    protected static final Color HLT_COLOR = Color.green;

    /** The stroke object used to draw the highlighted tile. */
    protected static final Stroke HLT_STROKE = new BasicStroke(3);

    /** The currently highlighted tile. */
    protected Point _htile;

    /** The font to draw tile coordinates. */
    protected Font _font;

    /** The dirty tile row segments that need to be re-painted. */
    protected ArrayList _dirty;

    /** The scene model data. */
    protected IsoSceneModel _model;

    /** The scene object to be displayed. */
    protected Scene _scene;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The tile manager. */
    protected TileManager _tilemgr;
}
