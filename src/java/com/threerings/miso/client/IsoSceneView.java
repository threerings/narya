//
// $Id: IsoSceneView.java,v 1.62 2001/10/17 23:39:06 shaper Exp $

package com.threerings.miso.scene;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.util.List;
import java.util.*;

import com.samskivert.util.HashIntMap;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.ObjectTile;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.*;

/**
 * The <code>IsoSceneView</code> provides an isometric view of a
 * particular scene.
 */
public class IsoSceneView implements SceneView
{
    /**
     * Construct an <code>IsoSceneView</code> object.
     *
     * @param spritemgr the sprite manager.
     * @param model the data model.
     */
    public IsoSceneView (SpriteManager spritemgr, IsoSceneViewModel model)
    {
        _spritemgr = spritemgr;

        _model = model;
        _model.precalculate();

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
    }

    // documentation inherited
    public void setScene (MisoScene scene)
    {
        _scene = scene;

        // clear all dirty lists and tile array
	clearDirtyRegions();

        // generate all object shadow tiles and polygons
        initAllObjectBounds();

        // invalidate the entire screen as there's a new scene in town
        invalidate();
    }

    // documentation inherited
    public void paint (Graphics g)
    {
	if (_scene == null) {
            Log.info("Scene view painted with null scene.");
            return;
        }

	Graphics2D gfx = (Graphics2D)g;

	// clip the drawing region to our desired bounds since we
	// currently draw tiles willy-nilly in undesirable areas.
    	Shape oldclip = gfx.getClip();
    	gfx.setClip(0, 0, _model.bounds.width, _model.bounds.height);

	if (_numDirty == 0) {
            // invalidate the entire screen
            invalidate();
	}

        // render the scene to the graphics context
        renderScene(gfx);

        // draw frames of dirty tiles and rectangles
        // drawDirtyRegions(gfx);

        // draw tile coordinates
        if (_model.showCoords) {
            paintCoordinates(gfx);
        }

        // clear out the dirty tiles and rectangles
        clearDirtyRegions();

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

    /**
     * Invalidate the entire visible scene view.
     */
    protected void invalidate ()
    {
        DirtyRectList rects = new DirtyRectList();
        rects.add(new Rectangle(
            0, 0, _model.bounds.width,_model.bounds.height));
        invalidateRects(rects);
    }

    /**
     * Clears the dirty rectangles and items lists, and the array of
     * dirty tiles.
     */
    protected void clearDirtyRegions ()
    {
	_dirtyRects.clear();
        _dirtyItems.clear();

	_numDirty = 0;
	for (int xx = 0; xx < _model.scenewid; xx++) {
	    for (int yy = 0; yy < _model.scenehei; yy++) {
		_dirty[xx][yy] = false;
	    }
	}
    }

    /**
     * Draws highlights around the dirty tiles and rectangles.
     */
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
    protected void renderScene (Graphics2D gfx)
    {
        renderTiles(gfx);
        renderDirtyItems(gfx);
    }

    /**
     * Renders the base and fringe layer tiles to the given graphics
     * context.
     */
    protected void renderTiles (Graphics2D gfx)
    {
        Tile[][][] tiles = _scene.getTiles();

        // render the base and fringe layers
	for (int yy = 0; yy < _model.scenehei; yy++) {
	    for (int xx = 0; xx < _model.scenewid; xx++) {
		if (_dirty[xx][yy]) {

                    // draw both layers at this tile position
                    for (int kk = MisoScene.LAYER_BASE;
                         kk < MisoScene.LAYER_FRINGE; kk++) {

                        // get the tile at these coordinates and layer
                        Tile tile = tiles[kk][xx][yy];
                        if (tile != null) {
                            // draw the tile image
                            tile.paint(gfx, _polys[xx][yy]);
                        }
                    }

                }
	    }
	}
    }

    /**
     * Renders the dirty sprites and objects in the scene to the given
     * graphics context.
     */
    protected void renderDirtyItems (Graphics2D gfx)
    {
        // sort the dirty sprites and objects visually back-to-front
        _dirtyItems.sort(IsoUtil.DIRTY_COMP);

        // render dirty sprites and objects
        int size = _dirtyItems.size();
        for (int ii = 0; ii < size; ii++) {
            ((DirtyItemList.DirtyItem)_dirtyItems.get(ii)).paint(gfx);
        }
    }

    /**
     * Generates and stores bounding polygons for all object tiles in
     * the scene for later use while rendering.
     */
    protected void initAllObjectBounds ()
    {
        // clear out any previously existing object polygons
        _objpolys.clear();

        // generate bounding polygons for all objects
        ObjectTile[][] tiles = _scene.getObjectLayer();
        for (int xx = 0; xx < _model.scenewid; xx++) {
            for (int yy = 0; yy < _model.scenehei; yy++) {
                ObjectTile tile = tiles[xx][yy];
                if (tile != null) {
                    generateObjectBounds(tile, xx, yy);
                }
            }
        }
    }

    /**
     * Generates and stores the bounding polygon for the object which
     * is used when invalidating dirty rectangles or tiles, and when
     * rendering the object to a graphics context.  This method should
     * be called when an object tile is added to a scene.
     */
    protected void generateObjectBounds (ObjectTile tile, int x, int y)
    {
        // create the bounding polygon for this object
        int key = getCoordinateKey(x, y);
        Polygon bounds = IsoUtil.getObjectBounds(_model, _polys[x][y], tile);

        // save it off in the object bounds hashtable
        _objpolys.put(key, bounds);
    }

    /**
     * Returns a unique integer key corresponding to the given
     * coordinates, suitable for storing and retrieving objects from a
     * hashtable.
     */
    protected int getCoordinateKey (int x, int y)
    {
        return (x << 16 | y);
    }

    /**
     * Paints tile coordinate numbers on all dirty tiles.
     *
     * @param gfx the graphics context.
     */
    protected void paintCoordinates (Graphics2D gfx)
    {
        FontMetrics fm = gfx.getFontMetrics(_font);

	gfx.setFont(_font);
	gfx.setColor(Color.white);

        int cx = _model.tilehwid, cy = _model.tilehhei;
        int fhei = fm.getAscent();

        for (int yy = 0; yy < _model.scenehei; yy++) {
            for (int xx = 0; xx < _model.scenewid; xx++) {
                if (_dirty[xx][yy]) {

                    // get the top-left screen coordinates of the tile
                    Rectangle bounds = _polys[xx][yy].getBounds();
                    int sx = bounds.x, sy = bounds.y;

                    // draw x-coordinate
                    String str = "" + xx;
                    int xpos = sx + cx - (fm.stringWidth(str) / 2);
                    gfx.drawString(str, xpos, sy + cy);

                    // draw y-coordinate
                    str = "" + yy;
                    xpos = sx + cx - (fm.stringWidth(str) / 2);
                    gfx.drawString(str, xpos, sy + cy + fhei);

                }
            }
        }
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

    // documentation inherited
    public void invalidateRects (DirtyRectList rects)
    {
        // we specifically need to allow the dirty rects list to grow
        // while we're iterating over it, so we're sure to call
        // rects.size() each time through the loop
        for (int ii = 0; ii < rects.size(); ii++) {
            Rectangle r = (Rectangle)rects.get(ii);

	    // dirty the tiles impacted by this rectangle
	    invalidateScreenRect(rects, r.x, r.y, r.width, r.height);

            // dirty any sprites or objects impacted by this rectangle
            invalidateItems(r);

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

    /**
     * Marks the tile at the given coordinates dirty and adds dirty
     * rectangles to the dirty rectangle list for any sprites
     * currently occupying the tile.
     */
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
	if (_dirty[x][y]) {
	    return;
	}

	// mark the tile dirty
	_numDirty++;
	_dirty[x][y] = true;

        // add the dirty rectangles of any sprites that we've just
        // inadvertently touched by dirtying this tile
        invalidateIntersectingSprites(rects, _polys[x][y]);

        // similarly, add any objects that we've just touched to the
        // dirty item list
        invalidateIntersectingObjects(rects, _polys[x][y]);
    }

    /**
     * Adds any sprites or objects in the scene whose bounds overlap
     * with the given dirty rectangle to the dirty item list for later
     * re-rendering.
     */
    protected void invalidateItems (Rectangle r)
    {
        // add any sprites impacted by the dirty rectangle
        _dirtySprites.clear();
        _spritemgr.getIntersectingSprites(_dirtySprites, r);

        int size = _dirtySprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_dirtySprites.get(ii);
            Point tpos = new Point();
            IsoUtil.screenToTile(_model, sprite.getX(), sprite.getY(), tpos);

            if (_dirtyItems.appendDirtySprite(sprite, tpos.x, tpos.y)) {
                // Log.info("Dirtied item: " + sprite);
            }
        }

        // add any objects impacted by the dirty rectangle
        ObjectTile tiles[][] = _scene.getObjectLayer();
        Iterator iter = _objpolys.keys();
        while (iter.hasNext()) {
            // get the object's coordinates and bounding polygon
            int coord = ((Integer)iter.next()).intValue();
            Polygon poly = (Polygon)_objpolys.get(coord);

            if (poly.intersects(r)) {
                int tx = coord >> 16, ty = coord & 0x0000FFFF;
                if (_dirtyItems.appendDirtyObject(
                    tiles[tx][ty], poly, tx, ty)) {
                    // Log.info("Dirtied item: Object(" + tx + ", " +
                    // ty + ")");
                }
            }
        }
    }

    /**
     * Adds dirty rectangles to the dirty rectangle list for any
     * sprites in the scene whose bounding rectangle overlaps with the
     * given shape.
     */
    protected void invalidateIntersectingSprites (
        DirtyRectList rects, Shape bounds)
    {
        _dirtySprites.clear();
        _spritemgr.getIntersectingSprites(_dirtySprites, bounds);

        int size = _dirtySprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_dirtySprites.get(ii);
            if (rects.appendDirtyRect(sprite.getRenderedBounds())) {
                // Log.info("Expanded for: " + sprite);
            }
        }
    }

    /**
     * Adds dirty rectangles to the dirty rectangle list for any
     * objects in the scene whose bounding rectangle overlaps with the
     * given shape's bounding rectangle.
     */
    protected void invalidateIntersectingObjects (
        DirtyRectList rects, Shape bounds)
    {
        ObjectTile tiles[][] = _scene.getObjectLayer();
        Iterator iter = _objpolys.keys();
        while (iter.hasNext()) {
            // get the object's coordinates and bounding polygon
            int coord = ((Integer)iter.next()).intValue();
            Polygon poly = (Polygon)_objpolys.get(coord);

            if (poly.intersects(bounds.getBounds())) {
                if (rects.appendDirtyRect(poly.getBounds())) {
                    // Log.info("Expanded for: " + poly.getBounds());
                }
            }
        }
    }

    // documentation inherited
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
		_scene.getBaseLayer(), _model.scenewid, _model.scenehei,
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
    protected Font _font = new Font("Arial", Font.PLAIN, 7);

    /** Polygon instances for all of our tiles. */
    protected Polygon _polys[][];

    /** Bounding polygons for all of the object tiles. */
    protected HashIntMap _objpolys = new HashIntMap();

    /** The dirty tiles that need to be re-painted. */
    protected boolean _dirty[][];

    /** The number of dirty tiles. */
    protected int _numDirty;

    /** The dirty rectangles that need to be re-painted. */
    protected ArrayList _dirtyRects = new ArrayList();

    /** The dirty sprites and objects that need to be re-painted. */
    protected DirtyItemList _dirtyItems = new DirtyItemList();

    /** The working sprites list used when calculating dirty regions. */
    protected ArrayList _dirtySprites = new ArrayList();

    /** The scene view model data. */
    protected IsoSceneViewModel _model;

    /** The scene object to be displayed. */
    protected MisoScene _scene;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;
}
