//
// $Id: IsoSceneView.java,v 1.56 2001/09/28 01:31:32 mdb Exp $

package com.threerings.miso.scene;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.util.List;
import java.util.ArrayList;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.AStarPathUtil;
import com.threerings.miso.scene.util.IsoUtil;
import com.threerings.miso.scene.util.MisoSceneUtil;

/**
 * The <code>IsoSceneView</code> provides an isometric view of a
 * particular scene.
 */
public class IsoSceneView implements SceneView
{
    /**
     * Construct an <code>IsoSceneView</code> object.
     *
     * @param tilemgr the tile manager.
     * @param spritemgr the sprite manager.
     * @param model the data model.
     */
    public IsoSceneView (TileManager tilemgr, SpriteManager spritemgr,
                         IsoSceneViewModel model)
    {
	_tilemgr = tilemgr;
        _spritemgr = spritemgr;

        setModel(model);

        // get the font used to render tile coordinates
	_font = new Font("Arial", Font.PLAIN, 7);

        // create our polygon arrays and create polygons for each of the
        // tiles. we use these repeatedly, so we go ahead and make 'em all
        // up front
        _polys = new Polygon[model.scenewid][model.scenehei];
	for (int xx = 0; xx < model.scenewid; xx++) {
	    for (int yy = 0; yy < model.scenehei; yy++) {
		_polys[xx][yy] = IsoUtil.getTilePolygon(_model, xx, yy);
	    }
	}

        // create the array used to mark dirty tiles
        _dirty = new boolean[model.scenewid][model.tilehei];

	// create the list of dirty rectangles
	_dirtyRects = new ArrayList();

	clearDirtyRegions();
    }

    // documentation inherited
    public void setScene (MisoScene scene)
    {
        _scene = scene;
    }

    /**
     * Paint the scene view and any highlighted tiles to the given
     * graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g)
    {
	if (_scene == null) {
            return;
        }

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
	    // drawDirtyRegions(gfx);

	    // clear out the dirty tiles and rectangles
	    clearDirtyRegions();
	}

	// draw sprite paths
	if (_model.showPaths) {
	    _spritemgr.renderSpritePaths(gfx);
	}

	// draw marks at each location
	if (_model.showLocs) {
	    paintLocations(gfx);
	}

        // paint any extra goodies
	paintExtras(gfx);

	// restore the original clipping region
	gfx.setClip(oldclip);
    }

    /**
     * A function where derived classes can paint extra stuff while we've
     * got the clipping region set up.
     */
    protected void paintExtras (Graphics2D g)
    {
    }

    protected void clearDirtyRegions ()
    {
	_dirtyRects.clear();

	_numDirty = 0;
	for (int xx = 0; xx < _model.scenewid; xx++) {
	    for (int yy = 0; yy < _model.scenehei; yy++) {
		_dirty[xx][yy] = false;
	    }
	}
    }

    protected void drawDirtyRegions (Graphics2D gfx)
    {
	// draw the dirty tiles
	gfx.setColor(Color.cyan);
	for (int xx = 0; xx < _model.scenewid; xx++) {
	    for (int yy = 0; yy < _model.scenehei; yy++) {
		if (_dirty[xx][yy]) {
		    gfx.draw(_polys[xx][yy]);
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
        Tile[][][] tiles = _scene.getTiles();

	for (int yy = 0; yy < _model.scenehei; yy++) {
	    for (int xx = 0; xx < _model.scenewid; xx++) {

		// skip this tile if it's not marked dirty
		if (!_dirty[xx][yy]) continue;

		// get the tile's screen position
		Polygon poly = _polys[xx][yy];

		// draw all layers at this tile position
		for (int kk = 0; kk < MisoScene.NUM_LAYERS; kk++) {

		    // get the tile at these coordinates and layer
		    Tile tile = tiles[xx][yy][kk];
		    if (tile == null) continue;

		    // offset the image y-position by the tile-specific height
		    int ypos = poly.ypoints[0] - _model.tilehhei -
			(tile.height - _model.tilehei);

		    // draw the tile image
		    gfx.drawImage(tile.img, poly.xpoints[0], ypos, null);
		}

		// draw all sprites residing in the current tile
		_spritemgr.renderSprites(gfx, poly);

		// paint the tile coordinate if desired
  		if (_model.showCoords) {
                    paintCoords(gfx, xx, yy, poly.xpoints[0],
				poly.ypoints[0] - _model.tilehhei);
                }

		// bail early if we know we've drawn all dirty tiles
		if (++numDrawn == _numDirty) break;
	    }
	}
    }

    /**
     * Render the scene to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    protected void renderScene (Graphics2D gfx)
    {
        Tile[][][] tiles = _scene.getTiles();
	int mx = 1;
	int my = 0;

	int screenY = _model.origin.y;

	for (int ii = 0; ii < _model.tilerows; ii++) {
	    // determine starting tile coordinates
	    int tx = (ii < _model.scenehei) ? 0 : mx++;
	    int ty = my;

	    // determine number of tiles in this row
	    int length = (ty - tx) + 1;

	    // determine starting screen x-position
	    int screenX = _model.origin.x - ((length) * _model.tilehwid);

	    for (int jj = 0; jj < length; jj++) {

		for (int kk = 0; kk < MisoScene.NUM_LAYERS; kk++) {
		    // grab the tile we're rendering
		    Tile tile = tiles[tx][ty][kk];
		    if (tile == null) continue;

		    // determine screen y-position, accounting for
		    // tile image height
		    int ypos = screenY - (tile.height - _model.tilehei);

		    // draw the tile image at the appropriate screen position
		    gfx.drawImage(tile.img, screenX, ypos, null);

                    // draw all sprites residing in the current tile
                    // TODO: simplify other tile positioning here to use poly
                    _spritemgr.renderSprites(gfx, _polys[tx][ty]);
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
	    if ((++my) > _model.scenehei - 1) {
                my = _model.scenehei - 1;
            }
	}
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
     * Paint demarcations at all locations in the scene, with each
     * location's cluster index, if any, along the right side of its
     * rectangle.
     *
     * @param gfx the graphics context.
     */
    protected void paintLocations (Graphics2D gfx)
    {
	List locations = _scene.getLocations();
	int size = locations.size();

	// create the location triangle
	Polygon tri = new Polygon();
	tri.addPoint(-3, -3);
	tri.addPoint(3, -3);
	tri.addPoint(0, 3);

	for (int ii = 0; ii < size; ii++) {

	    // retrieve the location
	    Location loc = (Location)locations.get(ii);

	    // get the cluster index this location is in, if any
	    int clusteridx = MisoSceneUtil.getClusterIndex(_scene, loc);

	    Point spos = new Point();
	    IsoUtil.fullToScreen(_model, loc.x, loc.y, spos);

	    int cx = spos.x, cy = spos.y;

	    // translate the origin to center on the location
	    gfx.translate(cx, cy);

	    // rotate to reflect the location orientation
	    double rot = (Math.PI / 4.0f) * loc.orient;
	    gfx.rotate(rot);

	    // draw the triangle
	    Color fcol = (loc instanceof Portal) ? Color.green : Color.yellow;
	    gfx.setColor(fcol);
	    gfx.fill(tri);

	    // outline the triangle in black
	    gfx.setColor(Color.black);
	    gfx.draw(tri);

	    // draw the rectangle
	    gfx.setColor(Color.red);
  	    gfx.fillRect(-1, 2, 3, 3);

	    // restore the original transform
	    gfx.rotate(-rot);
	    gfx.translate(-cx, -cy);

	    if (clusteridx != -1) {
		// draw the cluster index number on the right side
		gfx.setFont(_font);
		gfx.setColor(Color.white);
		gfx.drawString("" + clusteridx, cx + 5, cy + 3);
	    }
	}
    }

    /**
     * Invalidate a list of rectangles in the view for later repainting.
     *
     * @param rects the list of Rectangle objects.
     */
    public void invalidateRects (DirtyRectList rects)
    {
        // we specifically need to allow the dirty rects list to grow
        // while we're iterating over it, so we're sure to call
        // rects.size() each time through the loop
        for (int ii = 0; ii < rects.size(); ii++) {
            Rectangle r = (Rectangle)rects.get(ii);

	    // dirty the tiles impacted by this rectangle
	    invalidateScreenRect(rects, r.x, r.y, r.width, r.height);

	    // save the rectangle for potential display later
	    _dirtyRects.add(r);
	}
    }

    /**
     * Invalidate the specified rectangle in screen pixel coordinates
     * in the view.
     *
     * @param rects the dirty rectangle list that we're processing because
     * we may have to add dirty rectangles to it when invalidating this
     * particular rect.
     * @param x the rectangle x-position.
     * @param y the rectangle y-position.
     * @param width the rectangle width.
     * @param height the rectangle height.
     */
    public void invalidateScreenRect (
        DirtyRectList rects, int x, int y, int width, int height)
    {
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
		addDirtyTile(rects, tx++, ty--);
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
		addDirtyTile(rects, tx++, ty--);
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

    protected void addDirtyTile (DirtyRectList rects, int x, int y)
    {
	// constrain x-coordinate to a valid range
	if (x < 0) {
	    x = 0;
	} else if (x >= _model.scenewid) {
	    x = _model.scenewid - 1;
	}

	// constrain y-coordinate to a valid range
	if (y < 0) {
	    y = 0;
	} else if (y >= _model.scenehei) {
	    y = _model.scenehei - 1;
	}

	// do nothing if the tile's already dirty
	if (_dirty[x][y]) return;

	// mark the tile dirty
	_numDirty++;
	_dirty[x][y] = true;

        // and add the dirty rectangles of any sprites that we've just
        // inadvertently touched by dirtying this tile
        _spritemgr.invalidateIntersectingSprites(rects, _polys[x][y]);
    }

    public void setModel (IsoSceneViewModel model)
    {
        _model = model;
        _model.precalculate();
    }

    public Path getPath (AmbulatorySprite sprite, int x, int y)
    {
        // make sure the destination point is within our bounds
        if (x < 0 || x >= _model.bounds.width ||
            y < 0 || y >= _model.bounds.height) {
            return null;
        }

	// constrain destination pixels to fine coordinates
	Point fpos = new Point();
	IsoUtil.screenToFull(_model, x, y, fpos);

	// calculate tile coordinates for start and end position
	Point stpos = new Point();
	IsoUtil.screenToTile(_model, sprite.getX(), sprite.getY(), stpos);
	int tbx = IsoUtil.fullToTile(fpos.x);
	int tby = IsoUtil.fullToTile(fpos.y);

	// get a reasonable path from start to end
	List tilepath =
	    AStarPathUtil.getPath(
		_scene.getTiles(), _model.scenewid, _model.scenehei,
		sprite, stpos.x, stpos.y, tbx, tby);
	if (tilepath == null) {
	    return null;
	}

	// TODO: make more visually appealing path segments from start
	// to second tile, and penultimate to ultimate tile.

	// construct path with starting screen position
        LineSegmentPath path =
            new LineSegmentPath(sprite.getX(), sprite.getY());

	// add all nodes on the calculated path
	Point nspos = new Point();
	Point prev = stpos;
	int size = tilepath.size();
	for (int ii = 1; ii < size - 1; ii++) {
	    Point n = (Point)tilepath.get(ii);

	    // determine the direction this node lies in from the
	    // previous node
	    int dir = IsoUtil.getIsoDirection(prev.x, prev.y, n.x, n.y);

	    // determine the node's position in screen pixel coordinates 
	    IsoUtil.tileToScreen(_model, n.x, n.y, nspos);

	    // add the node to the path, wandering through the middle
	    // of each tile in the path for now
	    path.addNode(nspos.x + _model.tilehwid,
			 nspos.y + _model.tilehhei, dir);

	    prev = n;
	}

	// get the final destination point's screen coordinates
	// constrained to the closest full coordinate
	Point spos = new Point();
	IsoUtil.fullToScreen(_model, fpos.x, fpos.y, spos);

	// get the direction we're to face while heading toward the end
	int dir;
	if (prev == stpos) {
	    // if our destination is within our origination tile,
	    // direction is based on fine coordinates
	    dir = IsoUtil.getDirection(_model, sprite.getX(), sprite.getY(),
				       spos.x, spos.y);
	} else {
	    // else it's based on the last tile we traversed
	    dir = IsoUtil.getIsoDirection(prev.x, prev.y, tbx, tby);
	}

	// add the final destination path node
	path.addNode(spos.x, spos.y, dir);

        return path;
    }

    /** The font to draw tile coordinates. */
    protected Font _font;

    /** Polygon instances for all of our tiles. */
    protected Polygon _polys[][];

    /** The dirty tiles that need to be re-painted. */
    protected boolean _dirty[][];

    /** The number of dirty tiles. */
    protected int _numDirty;

    /** The dirty rectangles that need to be re-painted. */
    protected ArrayList _dirtyRects;

    /** The scene view model data. */
    protected IsoSceneViewModel _model;

    /** The scene object to be displayed. */
    protected MisoScene _scene;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The tile manager. */
    protected TileManager _tilemgr;
}
