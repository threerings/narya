//
// $Id: IsoSceneView.java,v 1.29 2001/08/08 03:19:38 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.Log;
import com.threerings.miso.sprite.*;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

/**
 * The <code>IsoSceneView</code> provides an isometric view of a
 * particular scene.
 */
public class IsoSceneView implements EditableSceneView
{
    /**
     * Construct an <code>IsoSceneView</code> object.
     *
     * @param tilemgr the tile manager.
     * @param spritemgr the sprite manager.
     * @param model the data model.
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

        // create the array used to mark dirty tiles
        _dirty = new boolean[Scene.TILE_WIDTH][Scene.TILE_HEIGHT];

	// create the list of dirty rectangles
	_dirtyRects = new ArrayList();

	clearDirtyRegions();
    }

    /**
     * Paint the scene view and any highlighted tiles to the given
     * graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g)
    {
	if (_scene == null) return;

	Graphics2D gfx = (Graphics2D)g;

	// clip the drawing region to our desired bounds since we
	// currently draw tiles willy-nilly in undesirable areas.
  	Shape oldclip = gfx.getClip();
  	gfx.setClip(0, 0, _model.bounds.width, _model.bounds.height);

	if (_numDirty == 0) {
	    // render the full scene
	    renderScene(gfx);

	} else {
	    // render only dirty tiles
	    renderSceneInvalid(gfx);

	    // draw frames of dirty tiles and rectangles
	    //drawDirtyRegions(gfx);

	    // clear out the dirty tiles and rectangles
	    clearDirtyRegions();
	}

        // draw an outline around the highlighted tile
        paintHighlightedTile(gfx, _htile.x, _htile.y);

        // draw lines illustrating tracking of the mouse position
  	//paintMouseLines(gfx);

	// restore the original clipping region
	gfx.setClip(oldclip);
    }

    protected void clearDirtyRegions ()
    {
	_dirtyRects.clear();

	_numDirty = 0;
	for (int xx = 0; xx < Scene.TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < Scene.TILE_HEIGHT; yy++) {
		_dirty[xx][yy] = false;
	    }
	}
    }

    protected void drawDirtyRegions (Graphics2D gfx)
    {
	// draw the dirty tiles
	gfx.setColor(Color.cyan);
	for (int xx = 0; xx < Scene.TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < Scene.TILE_HEIGHT; yy++) {
		if (_dirty[xx][yy]) {
		    gfx.draw(getTilePolygon(xx, yy));
		}
	    }
	}

	// draw the dirty rectangles
	gfx.setColor(Color.red);
	int size = _dirtyRects.size();
	for (int ii = 0; ii < size; ii++) {
	    Rectangle rect = (Rectangle)_dirtyRects.get(ii);
	    gfx.draw(rect);
	}
    }

    /**
     * Render the scene to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    protected void renderSceneInvalid (Graphics2D gfx)
    {
	int numDrawn = 0;

	for (int xx = 0; xx < Scene.TILE_WIDTH; xx++) {
	    for (int yy = 0; yy < Scene.TILE_HEIGHT; yy++) {

		// skip this tile if it's not marked dirty
		if (!_dirty[xx][yy]) continue;

		// get the tile's screen position
		Polygon poly = getTilePolygon(xx, yy);

		// draw all layers at this tile position
		for (int kk = 0; kk < Scene.NUM_LAYERS; kk++) {

		    // get the tile at these coordinates and layer
		    Tile tile = _scene.tiles[xx][yy][kk];
		    if (tile == null) continue;

		    // offset the image y-position by the tile-specific height
		    int ypos = poly.ypoints[0] - _model.tilehhei -
			(tile.height - _model.tilehei);

		    // draw the tile image
		    gfx.drawImage(tile.img, poly.xpoints[0], ypos, null);
		}

		// draw all sprites residing in the current tile
		_spritemgr.renderSprites(gfx, poly);

		// bail early if we know we've drawn all dirty tiles
		if (++numDrawn == _numDirty) break;
	    }
	}
    }

    /**
     * Return a polygon framing the specified tile.
     *
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     */
    protected Polygon getTilePolygon (int x, int y)
    {
        // get the top-left screen coordinate for the tile
        Point spos = new Point();
        IsoUtil.tileToScreen(_model, x, y, spos);

        // create a polygon framing the tile
        Polygon poly = new Polygon();
        poly.addPoint(spos.x, spos.y + _model.tilehhei);
        poly.addPoint(spos.x + _model.tilehwid, spos.y);
        poly.addPoint(spos.x + _model.tilewid, spos.y + _model.tilehhei);
        poly.addPoint(spos.x + _model.tilehwid, spos.y + _model.tilehei);
        poly.addPoint(spos.x, spos.y + _model.tilehhei);

        return poly;
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

                    // draw all sprites residing in the current tile
                    // TODO: simplify other tile positioning here to use poly
                    _spritemgr.renderSprites(gfx, getTilePolygon(tx, ty));
                }

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
        Point[] lx = _model.lineX;

	// draw the baseline x-axis line
	gfx.setColor(Color.red);
	gfx.drawLine(lx[0].x, lx[0].y, lx[1].x, lx[1].y);

	/*
	// draw line from last mouse pos to baseline
	gfx.setColor(Color.yellow);
	gfx.drawLine(ly[0].x, ly[0].y, ly[1].x, ly[1].y);

	// draw the most recent mouse cursor position
	gfx.setColor(Color.green);
	gfx.fillRect(ly[0].x, ly[0].y, 2, 2);
	gfx.setColor(Color.red);
	gfx.drawRect(ly[0].x - 1, ly[0].y - 1, 3, 3);
	*/
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
        FontMetrics fm = gfx.getFontMetrics(_font);

	gfx.setFont(_font);
	gfx.setColor(Color.white);

        int cx = _model.tilehwid, cy = _model.tilehhei;
        int fhei = fm.getAscent();

        // draw x-coordinate
        String str = "" + x;
        gfx.drawString(str, sx + cx - (fm.stringWidth(str)/2), sy + cy);

        // draw y-coordinate
        str = "" + y;
        gfx.drawString(str, sx + cx - (fm.stringWidth(str)/2), sy + cy + fhei);
    }

    /**
     * Paint a highlight around the specified tile.
     *
     * @param gfx the graphics context.
     * @param x the tile x-position coordinate.
     * @param y the tile y-position coordinate.
     */
    protected void paintHighlightedTile (Graphics2D gfx, int x, int y)
    {
        // set the desired stroke and color
	Stroke ostroke = gfx.getStroke();
	gfx.setStroke(HLT_STROKE);
	gfx.setColor(HLT_COLOR);

        // draw the tile outline
        gfx.draw(getTilePolygon(x, y));

        // restore the original stroke
	gfx.setStroke(ostroke);
    }

    public void setHighlightedTile (int sx, int sy)
    {
        IsoUtil.screenToTile(_model, sx, sy, _htile);
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

	    // dirty the tiles impacted by this rectangle
	    invalidateScreenRect(r.x, r.y, r.width, r.height);

	    // save the rectangle for potential display later
	    _dirtyRects.add(r);
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
	// TODO: fix boundary conditions, store contiguous dirty tile
	// row segments rather than individual tiles, use specialized
	// data structure rather than allocating int[] every time.

	// note that corner tiles may be included unnecessarily, but
	// checking to determine whether they're actually needed
	// complicates the code with likely-insufficient benefit

	// determine the top-left tile impacted by this rect
        Point tpos = new Point();
        IsoUtil.screenToTile(_model, x, y, tpos);

	// determine screen coordinates for top-left tile
	Point topleft = new Point();
	IsoUtil.tileToScreen(_model, tpos.x, tpos.y, topleft);

	// determine number of horizontal and vertical tiles for rect
	int numh = (int)Math.ceil((float)width / (float)_model.tilewid);
	int numv = (int)Math.ceil((float)height / (float)_model.tilehhei);

	// set up iterating variables
	int tx = tpos.x, ty = tpos.y, mx = tpos.x, my = tpos.y;;

	// set the starting screen y-position
	int screenY = topleft.y;

	// add top row if rect may overlap
	if (y < (screenY + _model.tilehhei)) {
	    ty--;
	    for (int ii = 0; ii < numh; ii++) {
		if (!_dirty[tx][ty]) _numDirty++;
		_dirty[tx++][ty--] = true;
	    }
	}

	// add rows to the bottom if rect may overlap
	int ypos = screenY + (numv * _model.tilehhei);
	if ((y + height) > ypos) {
	    numv += ((y + height) > (ypos + _model.tilehhei)) ? 2 : 1;
	}

	// add dirty tiles from each affected row
	boolean isodd = false;
	for (int ii = 0; ii < numv; ii++) {

	    // set up iterating variables for this row
	    tx = mx;
	    ty = my;
	    int length = numh;

	    // set the starting screen x-position
	    int screenX = topleft.x;
	    if (isodd) {
		screenX -= _model.tilehwid;
	    }

	    // skip leftmost tile if rect doesn't overlap
  	    if (x > screenX + _model.tilewid) {
  		tx++;
  		ty--;
		screenX += _model.tilewid;
  	    }

	    // add to the right edge if rect may overlap
	    if (x + width > (screenX + (length * _model.tilewid))) {
		length++;
	    }

	    // add all tiles in the row to the dirty set
	    for (int jj = 0; jj < length; jj++) {
		if (!_dirty[tx][ty]) _numDirty++;
		_dirty[tx++][ty--] = true;
	    }

	    // step along the x- or y-axis appropriately
	    if (isodd) {
		mx++;
	    } else {
		my++;
	    }

	    // increment the screen y-position
	    screenY += _model.tilehhei;

	    // toggle whether we're drawing an odd-numbered row
	    isodd = !isodd;
	}
    }

    public void setScene (Scene scene)
    {
	_scene = scene;
    }

    public boolean getShowCoordinates ()
    {
	return _model.showCoords;
    }

    public void setShowCoordinates (boolean show)
    {
	_model.showCoords = show;
    }

    public void setTile (int x, int y, int lnum, Tile tile)
    {
	Point tpos = new Point();
        IsoUtil.screenToTile(_model, x, y, tpos);
	_scene.tiles[tpos.x][tpos.y][lnum] = tile;
    }

    public void setModel (IsoSceneModel model)
    {
        _model = model;
        _model.calculateXAxis();
    }

    public Path getPath (Sprite sprite, int x, int y)
    {
        // make sure the destination point is within our bounds
        if (x < 0 || x >= _model.bounds.width ||
            y < 0 || y >= _model.bounds.height) {
            return null;
        }

        // create path from current loc to destination
        Path path = new Path(sprite.x, sprite.y);
	int dir = IsoUtil.getDirection(_model, sprite.x, sprite.y, x, y);
        path.addNode(x, y, dir);

        return path;
    }

    /** The color to draw the highlighted tile. */
    protected static final Color HLT_COLOR = Color.green;

    /** The stroke object used to draw the highlighted tile. */
    protected static final Stroke HLT_STROKE = new BasicStroke(3);

    /** The currently highlighted tile. */
    protected Point _htile;

    /** The font to draw tile coordinates. */
    protected Font _font;

    /** The dirty tiles that need to be re-painted. */
    protected boolean _dirty[][];

    /** The number of dirty tiles. */
    protected int _numDirty;

    /** The dirty rectangles that need to be re-painted. */
    protected ArrayList _dirtyRects;

    /** The scene model data. */
    protected IsoSceneModel _model;

    /** The scene object to be displayed. */
    protected Scene _scene;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The tile manager. */
    protected TileManager _tilemgr;
}
