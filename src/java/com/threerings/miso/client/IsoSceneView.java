//
// $Id: IsoSceneView.java,v 1.100 2002/02/19 05:03:17 mdb Exp $

package com.threerings.miso.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.StringUtil;

import com.threerings.media.animation.AnimationManager;
import com.threerings.media.sprite.Path;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;

import com.threerings.miso.Log;
import com.threerings.miso.scene.DirtyItemList.DirtyItem;
import com.threerings.miso.scene.util.AStarPathUtil;
import com.threerings.miso.scene.util.IsoUtil;

/**
 * The iso scene view provides an isometric view of a particular scene. It
 * presently supports scrolling in a limited form. Object tiles are not
 * handled properly, nor is mouse highlighting. Those should only be used
 * if the view will not be scrolled.
 */
public class IsoSceneView implements SceneView
{
    /** Instructs the scene view never to draw highlights around object
     * tiles. */
    public static final int HIGHLIGHT_NEVER = 0;

    /** Instructs the scene view to highlight only object tiles that have
     * a non-empty action string. */
    public static final int HIGHLIGHT_WITH_ACTION = 1;

    /** Instructs the scene view to highlight every object tile,
     * regardless of whether it has a valid action string. */
    public static final int HIGHLIGHT_ALWAYS = 2;

    /** Instructs the scene view to highlight whatever tile the mouse is
     * over, regardless of whether or not it is an object tile. This is
     * generally only useful in an editor rather than a game. */
    public static final int HIGHLIGHT_ALL = 3;

    /**
     * Constructs an iso scene view.
     *
     * @param animmgr the animation manager.
     * @param spritemgr the sprite manager.
     * @param model the data model.
     */
    public IsoSceneView (AnimationManager animmgr, SpriteManager spritemgr,
                         IsoSceneViewModel model)
    {
        // save off references
        _animmgr = animmgr;
        _spritemgr = spritemgr;
        _model = model;

        // create our polygon arrays, these will be populated with the
        // tile polygons as they are requested
        _polys = new Polygon[model.scenewid][model.scenehei];

        // create the array used to mark dirty tiles
        _dirty = new boolean[model.scenewid][model.tilehei];
    }

    /**
     * Configures the scene view to highlight object tiles either never
     * ({@link #HIGHLIGHT_NEVER}), only when an object tile has an
     * associated action string ({@link #HIGHLIGHT_WITH_ACTION}), or
     * always ({@link #HIGHLIGHT_ALWAYS}). It is also possible to
     * configure the view to highlight whatever tile is under the cursor,
     * even if it's not an object tile which is done in the {@link
     * #HIGHLIGHT_ALL} mode.
     */
    public void setHighlightMode (int hmode)
    {
        _hmode = hmode;
    }

    /**
     * Returns a reference to the scene being displayed by this view.
     */
    public DisplayMisoScene getScene ()
    {
        return _scene;
    }

    // documentation inherited
    public void setScene (DisplayMisoScene scene)
    {
        _scene = scene;

        // clear all dirty lists and tile array
	clearDirtyRegions();

        // obtain a list of the objects in the scene and generate records
        // for each of them that contain precomputed metrics
        prepareObjectList();

        // invalidate the entire screen as there's a new scene in town
        invalidate();
    }

    // documentation inherited from interface
    public void viewWillScroll (int dx, int dy)
    {
        // adjust our offsets
        _xoff += dx;
        _yoff += dy;

        // determine whether or not this scrolling causes us to cross
        // boundaries in the x or y directions
        if (_xoff >= _model.tilehwid) {
            _tiledx += 1;
            _tiledy -= 1;
            _xoff -= _model.tilewid;

        } else if (_xoff < -_model.tilehwid) {
            _tiledx -= 1;
            _tiledy += 1;
            _xoff += _model.tilewid;
        }

        if (_yoff >= _model.tilehhei) {
            _tiledx += 1;
            _tiledy += 1;
            _yoff -= _model.tilehei;

        } else if (_yoff < -_model.tilehhei) {
            _tiledx -= 1;
            _tiledy -= 1;
            _yoff += _model.tilehei;
        }
    }

    // documentation inherited from interface
    public void paint (Graphics2D gfx, List invalidRects)
    {
	if (_scene == null) {
            Log.warning("Scene view painted with null scene.");
            return;
        }

        // invalidate the invalid rectangles
        int rsize = invalidRects.size();
        for (int ii = 0; ii < rsize; ii++) {
            invalidateRect((Rectangle)invalidRects.get(ii));
        }

        // translate according to our scroll parameters
        gfx.translate(-_xoff, -_yoff);

        // render the scene to the graphics context
        renderScene(gfx);

        // draw tile coordinates
        if (_model.showCoords) {
            paintCoordinates(gfx);
        }

        // untranslate according to our scroll parameters
        gfx.translate(_xoff, _yoff);

        // draw frames of dirty tiles and rectangles
        // drawDirtyRegions(gfx);

        // render any animations
        _animmgr.renderAnimations(gfx);

        // clear out the dirty tiles and rectangles
        clearDirtyRegions();

	// draw sprite paths
	if (_model.showPaths) {
	    _spritemgr.renderSpritePaths(gfx);
	}

        // paint our highlighted tile (if any)
        paintHighlights(gfx);

        // paint any extra goodies
	paintExtras(gfx);
    }

    /**
     * Paints the highlighted tile.
     *
     * @param gfx the graphics context.
     */
    protected void paintHighlights (Graphics2D gfx)
    {
        // if we're not highlighting object tiles, bail now
        if (_hmode == HIGHLIGHT_NEVER) {
            return;
        }

        Polygon hpoly = null;

        // if the highlighted object is an object tile, we want to
        // highlight that
        if (_hobject instanceof ObjectTile) {
            // if we're only highlighting objects with actions, make sure
            // this one has an action
            String action = _scene.getObjectAction(_hcoords.x, _hcoords.y);
            if (_hmode != HIGHLIGHT_WITH_ACTION || !StringUtil.blank(action)) {
                Polygon tpoly = getTilePoly(_hcoords.x, _hcoords.y);
                hpoly = IsoUtil.getObjectFootprint(
                    _model, tpoly, (ObjectTile)_hobject);
            }
        }

        // if we have no highlight object, but we're in HIGHLIGHT_ALL,
        // then paint the bounds of the highlighted base tile
        if (hpoly == null && _hmode == HIGHLIGHT_ALL &&
            _hcoords.x != -1 && _hcoords.y != -1) {
            hpoly = getTilePoly(_hcoords.x, _hcoords.y);
        }

        // if we've determined that there's something to highlight
        if (hpoly != null) {
            // set the desired stroke and color
	    Stroke ostroke = gfx.getStroke();
	    gfx.setStroke(_hstroke);
	    gfx.setColor(Color.green);

	    // draw the outline
            gfx.draw(hpoly);

	    // restore the original stroke
	    gfx.setStroke(ostroke);
        }
    }

    /**
     * A function where derived classes can paint extra stuff while we've
     * got the clipping region set up.
     */
    protected void paintExtras (Graphics2D gfx)
    {
        // nothing for now
    }

    /**
     * Invalidate the entire visible scene view.
     */
    protected void invalidate ()
    {
        invalidateRect(_model.bounds);
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
            Arrays.fill(_dirty[xx], false);
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
		    gfx.draw(getTilePoly(xx, yy));
		}
	    }
	}

	// draw the dirty rectangles
        Stroke ostroke = gfx.getStroke();
        gfx.setStroke(DIRTY_RECT_STROKE);
	gfx.setColor(Color.red);
	int size = _dirtyRects.size();
	for (int ii = 0; ii < size; ii++) {
	    Rectangle rect = (Rectangle)_dirtyRects.get(ii);
	    gfx.draw(rect);
	}
        gfx.setStroke(ostroke);

        // draw the dirty item rectangles
        gfx.setColor(Color.yellow);
        size = _dirtyItems.size();
        for (int ii = 0; ii < size; ii++) {
            Rectangle rect = _dirtyItems.get(ii).dirtyRect;
            gfx.draw(rect);
        }
    }

    /**
     * Renders the scene to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    protected void renderScene (Graphics2D gfx)
    {
        // Log.info("renderScene");
        renderTiles(gfx);
        renderBaseDecorations(gfx);
        renderDirtyItems(gfx);
    }

    /**
     * Renders the base and fringe layer tiles to the given graphics
     * context.
     */
    protected void renderTiles (Graphics2D gfx)
    {
        // render the base and fringe layers
	for (int yy = 0; yy < _model.scenehei; yy++) {
	    for (int xx = 0; xx < _model.scenewid; xx++) {
 		if (!_dirty[xx][yy]) {
                    continue;
                }

                // offset the tile coordinates by our scrolled deltas
                int tx = xx + _tiledx, ty = yy + _tiledy;

                // draw the base and fringe tile images
                Tile tile;
                if ((tile = _scene.getBaseTile(tx, ty)) != null) {
                    tile.paint(gfx, getTilePoly(xx, yy));
                }
                if ((tile = _scene.getFringeTile(tx, ty)) != null) {
                    tile.paint(gfx, getTilePoly(xx, yy));
                }

                // if we're showing coordinates, outline the tiles as well
                if (_model.showCoords) {
                    gfx.draw(getTilePoly(xx, yy));
                }
	    }
	}
    }

    /**
     * A function where derived classes can paint things after the base
     * tiles have been rendered but before anything else has been rendered
     * (so that whatever is painted appears to be on the ground).
     */
    protected void renderBaseDecorations (Graphics2D gfx)
    {
        // nothing for now
    }

    /**
     * Renders the dirty sprites and objects in the scene to the given
     * graphics context.
     */
    protected void renderDirtyItems (Graphics2D gfx)
    {
//         Log.info("renderDirtyItems [rects=" + _dirtyRects.size() +
//                  ", items=" + _dirtyItems.size() + "].");

        // sort the dirty sprites and objects visually back-to-front
        DirtyItem items[] = _dirtyItems.sort();

        // render each item clipping to its dirty rectangle
        for (int ii = 0; ii < items.length; ii++) {
            items[ii].paint(gfx, items[ii].dirtyRect);
            // Log.info("Painting item [item=" + items[ii] + "].");
        }
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
                // if the tile's not dirty, don't paint the coordinates
                if (!_dirty[xx][yy]) {
                    continue;
                }

                // get the top-left screen coordinates of the tile
                Rectangle bounds = getTilePoly(xx, yy).getBounds();
                int sx = bounds.x, sy = bounds.y;

                // draw x-coordinate
                String str = String.valueOf(xx + _tiledx);
                int xpos = sx + cx - (fm.stringWidth(str) / 2);
                gfx.drawString(str, xpos, sy + cy);

                // draw y-coordinate
                str = String.valueOf(yy + _tiledy);
                xpos = sx + cx - (fm.stringWidth(str) / 2);
                gfx.drawString(str, xpos, sy + cy + fhei);
            }
        }
    }

    /**
     * Generates and stores bounding polygons for all object tiles in the
     * scene for later use while rendering.
     */
    protected void prepareObjectList ()
    {
        // clear out any previously existing object data
        _objects.clear();

        // generate metric records for all objects
        for (int yy = 0; yy < _model.scenehei; yy++) {
            for (int xx = 0; xx < _model.scenewid; xx++) {
                ObjectTile tile = _scene.getObjectTile(xx, yy);
                if (tile != null) {
                    generateObjectMetrics(tile, xx, yy);
                }
            }
        }
    }

    /**
     * Generates object tile metrics for the supplied object tile and adds
     * them to the list.
     */
    protected void generateObjectMetrics (ObjectTile tile, int x, int y)
    {
        // create a metrics record for this object
        ObjectMetrics metrics = new ObjectMetrics();
        metrics.tile = tile;
        metrics.x = x;
        metrics.y = y;
        metrics.bounds = IsoUtil.getObjectBounds(
            _model, getTilePoly(x, y), tile);

        // and add it to the list
        _objects.add(metrics);
    }

    /**
     * Clears out the object metrics for the object at the specified tile
     * coordinates.
     */
    protected void clearObjectMetrics (int x, int y)
    {
        for (int i = 0; i < _objects.size(); i++) {
            ObjectMetrics metrics = (ObjectMetrics)_objects.get(i);
            if (metrics.x == x && metrics.y == y) {
                _objects.remove(i);
                return;
            }
        }
    }

    /**
     * Returns the polygon bounding the tile at the specified coordinates.
     */
    protected Polygon getTilePoly (int x, int y)
    {
        Polygon poly = _polys[x][y];
        if (poly == null) {
            poly = _polys[x][y] = IsoUtil.getTilePolygon(_model, x, y);
        }
        return poly;
    }

    /**
     * Invalidates the specified rectangle in preparation for
     * rendering. Items that overlap the rectangle as well as tiles that
     * overlap the rectangle will be marked as needing to be rerendered.
     */
    protected void invalidateRect (Rectangle rect)
    {
        // dirty the tiles impacted by this rectangle
        Rectangle tileBounds = invalidateScreenRect(rect);

        // dirty any sprites or objects impacted by this rectangle
        invalidateItems(tileBounds);

        // save the rectangle for potential display later
        _dirtyRects.add(rect);
    }

    /**
     * Invalidates the given rectangle in screen pixel coordinates in the
     * view. Returns a rectangle that bounds all tiles that were dirtied.
     *
     * @param rect the dirty rectangle.
     */
    protected Rectangle invalidateScreenRect (Rectangle r)
    {
//         Log.info("Invalidating [rect=" + r +
//                  ", xoff=" + _xoff + ", yoff=" + _yoff + "].");

        // account for our current scrolling offset
        int rx = r.x + _xoff, ry = r.y + _yoff;

        // initialize the rectangle bounding all tiles dirtied by the
        // invalidated rectangle
        Rectangle tileBounds = new Rectangle(-1, -1, 0, 0);

	// note that corner tiles may be included unnecessarily, but
	// checking to determine whether they're actually needed
	// complicates the code with likely-insufficient benefit

	// determine the top-left tile impacted by this rect
        Point tpos = new Point();
        IsoUtil.screenToTile(_model, rx, ry, tpos);

	// determine screen coordinates for top-left tile
	Point topleft = new Point();
	IsoUtil.tileToScreen(_model, tpos.x, tpos.y, topleft);

	// determine number of horizontal tiles for rect
	int numh = (int)Math.ceil((float)r.width / (float)_model.tilewid);

	// set up iterating variables
	int tx = tpos.x, ty = tpos.y, mx = tpos.x, my = tpos.y;

	// set the starting screen y-position
	int screenY = topleft.y;

	// add top row if rect may overlap
	if (ry < (screenY + _model.tilehhei)) {
	    ty--;
	    for (int ii = 0; ii < numh; ii++) {
		addDirtyTile(tileBounds, tx++, ty--);
	    }
	}

	// determine the bottom-left tile impacted by this rect
        Point bpos = new Point();
        IsoUtil.screenToTile(_model, rx, ry + r.height, bpos);

	// determine screen coordinates for bottom-left tile
	Point botleft = new Point();
	IsoUtil.tileToScreen(_model, bpos.x, bpos.y, botleft);

        // determine the number of vertical rows for our rect (we do this
        // by subtracting the "height" of the top tile from that of the
        // bottom tile, the height being the sum of the x and y
        // coordinate)
	int numv = (bpos.x + bpos.y) - (tpos.x + tpos.y);

        // now we need to extend the rect to contain the row containing
        // the bottom tile, and potentially the row below that
        numv += ((ry + r.height) > (botleft.y + _model.tilehhei)) ? 2 : 1;

	// add dirty tiles from each affected row
	for (int ii = 0; ii < numv; ii++) {

	    // set up iterating variables for this row
	    tx = mx;
	    ty = my;
	    int length = numh;

	    // set the starting screen x-position
	    int screenX = topleft.x;
	    if (ii%2 == 1) {
		screenX -= _model.tilehwid;
	    }

	    // skip leftmost tile if rect doesn't overlap
  	    if (rx > screenX + _model.tilewid) {
  		tx++;
  		ty--;
		screenX += _model.tilewid;
  	    }

	    // add to the right edge if rect may overlap
	    if (rx + r.width > (screenX + (length * _model.tilewid))) {
		length++;
	    }

	    // add all tiles in the row to the dirty set
	    for (int jj = 0; jj < length; jj++) {
		addDirtyTile(tileBounds, tx++, ty--);
	    }

	    // step along the x- or y-axis appropriately
	    if (ii%2 == 1) {
		mx++;
	    } else {
		my++;
	    }
	}

        return tileBounds;
    }

    /**
     * Marks the tile at the given coordinates dirty and expands the tile
     * bounds rectangle to include the rectangle for the dirtied tile.
     */
    protected boolean addDirtyTile (Rectangle tileBounds, int x, int y)
    {
        if (!_model.isCoordinateValid(x, y)) {
            return false;
        }

        // expand the tile bounds rectangle to include this tile
        Rectangle bounds = getTilePoly(x, y).getBounds();
        if (tileBounds.x == -1) {
            tileBounds.setBounds(bounds);
        } else {
            tileBounds.add(bounds);
        }

	// do nothing if the tile's already dirty
	if (_dirty[x][y]) {
	    return false;
	}

	// mark the tile dirty
	_numDirty++;
	_dirty[x][y] = true;
        return true;
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
            MisoCharacterSprite sprite =
                (MisoCharacterSprite)_dirtySprites.get(ii);

            // get the dirty portion of the sprite
            Rectangle drect = sprite.getBounds().intersection(r);

            _dirtyItems.appendDirtySprite(
                sprite, sprite.getTileX(), sprite.getTileY(), drect);
            // Log.info("Dirtied item: " + sprite);
        }

        // add any objects impacted by the dirty rectangle
        if (_scene != null) {
            Iterator iter = _objects.iterator();
            while (iter.hasNext()) {
                ObjectMetrics metrics = (ObjectMetrics)iter.next();
                Polygon poly = metrics.bounds;
                if (!poly.intersects(r)) {
                    continue;
                }

                // get the dirty portion of the object
                Rectangle drect = poly.getBounds().intersection(r);
                int tx = metrics.x, ty = metrics.y;

                // compute the footprint if we're rendering those
                Polygon foot = null;
                if (_model.showFootprints) {
                    foot = IsoUtil.getObjectFootprint(
                        _model, getTilePoly(tx, ty), metrics.tile);
                }

                // add the intersected section of the object to the dirty
                // items list
                _dirtyItems.appendDirtyObject(
                    metrics.tile, poly, foot, tx, ty, drect);
                // Log.info("Dirtied item: Object(" + tx + ", " + ty + ")");
            }
        }
    }

    // documentation inherited
    public Path getPath (MisoCharacterSprite sprite, int x, int y)
    {
        // make sure we have a scene
        if (_scene == null) {
            return null;
        }

        // make sure the destination point is within our bounds
        if (!_model.bounds.contains(x, y)) {
            return null;
        }

        // get the destination tile coordinates
        Point dest = new Point();
        IsoUtil.screenToTile(_model, x + _xoff, y + _yoff, dest);

        // get a reasonable tile path through the scene
	List points = AStarPathUtil.getPath(
            _scene, _model.scenewid, _model.scenehei,
            sprite, sprite.getTileX(), sprite.getTileY(), dest.x, dest.y);

	// construct a path object to guide the sprite on its merry way
        return (points == null) ? null :
            new TilePath(_model, sprite, points, x, y);
    }

    // documentation inherited
    public Point getScreenCoords (int x, int y)
    {
	Point coords = new Point();
	IsoUtil.fullToScreen(_model, x, y, coords);
        // adjust for our scrolling offset
        coords.x -= _xoff;
        coords.y -= _yoff;
        return coords;
    }

    // documentation inherited
    public Point getFullCoords (int x, int y)
    {
        Point coords = new Point();
        IsoUtil.screenToFull(_model, x + _xoff, y + _yoff, coords);
        return coords;
    }

    // documentation inherited
    public boolean mouseMoved (MouseEvent e)
    {
        int x = e.getX(), y = e.getY();
        boolean repaint = false;

        // update the base tile coordinates that the mouse is over (if
        // it's also over an object tile, we'll override these values)
        if (_hmode == HIGHLIGHT_ALL) {
            repaint = (updateTileCoords(x, y, _hcoords) || repaint);
        }

        // compute the list of objects over which the mouse is hovering
        _hitList.clear();
        _hitSprites.clear();
        // add the sprites that contain the point
        _spritemgr.getHitSprites(_hitSprites, x, y);
        int hslen = _hitSprites.size();
        for (int i = 0; i < hslen; i++) {
            MisoCharacterSprite sprite =
                (MisoCharacterSprite)_hitSprites.get(i);
            _hitList.appendDirtySprite(
                sprite, sprite.getTileX(), sprite.getTileY(),
                sprite.getBounds());
        }

        // add the object tiles that contain the point
        getHitObjects(_hitList, x, y);

        // sort the list of hit items by rendering order
        Object hobject = null;
        DirtyItem[] items = _hitList.sort();
        // the last element in the array is what we want (assuming there
        // are any items in the array)
        if (items.length > 0) {
            hobject = items[items.length-1].obj;
        }

        // if this is an object tile, we need to update the hcoords
        if (hobject != null && hobject instanceof ObjectTile) {
            DirtyItem item = items[items.length-1];
            _hcoords.x = item.ox;
            _hcoords.y = item.oy;
        }

        // if this hover object is different than before, we'll need to be
        // repainted
        if (hobject != _hobject) {
            _hobject = hobject;

            // we need to repaint if we're highlighting objects, but if
            // we're only highlighting objects with actions, we only need
            // to repaint if the object has an action
            if (_hmode != HIGHLIGHT_NEVER) {
                repaint = (_hmode == HIGHLIGHT_WITH_ACTION) ?
                    (_scene.getObjectAction(_hcoords.x, _hcoords.y) != null) :
                    true;
            }
        }

        return repaint;
    }

    /**
     * Adds to the supplied dirty item list, all of the object tiles that
     * are hit by the specified point (meaning the point is contained
     * within their bounds and intersects a non-transparent pixel in the
     * actual object image.
     */
    protected void getHitObjects (DirtyItemList list, int x, int y)
    {
        Iterator iter = _objects.iterator();
        while (iter.hasNext()) {
            ObjectMetrics metrics = (ObjectMetrics)iter.next();
            // skip polys that don't contain the point
            if (!metrics.bounds.contains(x, y)) {
                continue;
            }

            // now check that the pixel in the tile image is
            // non-transparent at that point
            int tx = metrics.x, ty = metrics.y;
            Rectangle pbounds = metrics.bounds.getBounds();
            if (!metrics.tile.hitTest(x - pbounds.x, y - pbounds.y)) {
                continue;
            }

            // we've passed the test, add the object to the list
            list.appendDirtyObject(metrics.tile, metrics.bounds, null,
                                   tx, ty, pbounds);
        }
    }

    // documentation inherited
    public void mouseExited (MouseEvent e)
    {
        // clear the highlight tracking data
        _hcoords.setLocation(-1, -1);
        _hobject = null;
    }

    // documentation inherited
    public Object getHoverObject ()
    {
        return _hobject;
    }

    /**
     * Returns the tile coordinates of the tile over which the mouse is
     * hovering (which are the origin coordinates in the case of an object
     * tile).
     */
    public Point getHoverCoords ()
    {
        return _hcoords;
    }

    /**
     * Converts the supplied screen coordinates into tile coordinates,
     * writing the values into the supplied {@link Point} instance and
     * returning true if the screen coordinates translated into a
     * different set of tile coordinates than were already contained in
     * the point (so that the caller can know to update a highlight, for
     * example).
     *
     * @return true if the tile coordinates have changed.
     */
    protected boolean updateTileCoords (int sx, int sy, Point tpos)
    {
	Point npos = new Point();
        IsoUtil.screenToTile(_model, sx + _xoff, sy + _yoff, npos);

        // make sure the new coordinate is both valid and different
        if (_model.isCoordinateValid(npos.x, npos.y) && !tpos.equals(npos)) {
            tpos.setLocation(npos.x, npos.y);
            return true;

        } else {
            return false;
        }
    }

    /**
     * A class used to cache necessary information on all object tiles in
     * the scene.
     */
    protected static class ObjectMetrics
    {
        /** The x and y tile coordinates of the object. */
        public int x, y;

        /** A reference to the object tile itself. */
        public ObjectTile tile;

        /** The object's bounding polygon. */
        public Polygon bounds;
    }

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The animation manager. */
    protected AnimationManager _animmgr;

    /** The scene view model data. */
    protected IsoSceneViewModel _model;

    /** The scene to be displayed. */
    protected DisplayMisoScene _scene;

    /** Polygon outlines for all of our base tiles. */
    protected Polygon _polys[][];

    /** Metric information for all of the object tiles. */
    protected ArrayList _objects = new ArrayList();

    /** The rendering offsets used to support scrolling. */
    protected int _xoff = 0, _yoff = 0;

    /** The offsets from (0, 0) in tile coordinates to which we have
     * scrolled. */
    protected int _tiledx, _tiledy;

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

    /** Used to collect the list of sprites "hit" by a particular mouse
     * location. */
    protected List _hitSprites = new ArrayList();

    /** The list that we use to track and sort the items over which the
     * mouse is hovering. */
    protected DirtyItemList _hitList = new DirtyItemList();

    /** The highlight mode. */
    protected int _hmode = HIGHLIGHT_NEVER;

    /** The coordinates of the currently highlighted base or object
     * tile. */
    protected Point _hcoords = new Point(-1, -1);

    /** The object that the mouse is currently hovering over. */
    protected Object _hobject;

    /** The font to draw tile coordinates. */
    protected Font _font = new Font("Arial", Font.PLAIN, 7);

    /** The stroke object used to draw highlighted tiles and coordinates. */
    protected BasicStroke _hstroke = new BasicStroke(2);

    /** The stroke used to draw dirty rectangles. */
    protected static final Stroke DIRTY_RECT_STROKE = new BasicStroke(2);
}
