//
// $Id: IsoSceneView.java,v 1.77 2001/12/14 23:31:04 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Font;
import java.awt.BasicStroke;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.HashIntMap;

import com.threerings.media.sprite.DirtyRectList;
import com.threerings.media.sprite.Path;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.ObjectTileLayer;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;

import com.threerings.miso.Log;
import com.threerings.miso.scene.DirtyItemList.DirtyItem;
import com.threerings.miso.scene.util.AStarPathUtil;
import com.threerings.miso.scene.util.IsoUtil;
import com.threerings.miso.tile.BaseTileLayer;

/**
 * The iso scene view provides an isometric view of a particular
 * scene.
 */
public class IsoSceneView implements SceneView
{
    /**
     * Constructs an iso scene view.
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
    public void setScene (DisplayMisoScene scene)
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
            Log.warning("Scene view painted with null scene.");
            return;
        }

	Graphics2D gfx = (Graphics2D)g;

	// clip everything to the overall scene view bounds
    	Shape oldclip = gfx.getClip();
    	gfx.setClip(_model.bounds);

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

        // paint any extra goodies
	paintExtras(gfx);

	// restore the original clipping region
	gfx.setClip(oldclip);
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
        DirtyRectList rects = new DirtyRectList();
        rects.add(_model.bounds);
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
            Rectangle rect = ((DirtyItem)_dirtyItems.get(ii)).dirtyRect;
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
        renderDirtyItems(gfx);
    }

    /**
     * Renders the base and fringe layer tiles to the given graphics
     * context.
     */
    protected void renderTiles (Graphics2D gfx)
    {
        BaseTileLayer base = _scene.getBaseLayer();
        TileLayer fringe = _scene.getFringeLayer();

        // render the base and fringe layers
	for (int yy = 0; yy < base.getHeight(); yy++) {
	    for (int xx = 0; xx < base.getWidth(); xx++) {
		if (!_dirty[xx][yy]) {
                    continue;
                }

                // draw the base and fringe tile images
                Tile tile;
                if ((tile = base.getTile(xx, yy)) != null) {
                    tile.paint(gfx, _polys[xx][yy]);
                }
                if ((tile = fringe.getTile(xx, yy)) != null) {
                    tile.paint(gfx, _polys[xx][yy]);
                }

                // if we're showing coordinates, outline the tiles as well
                if (_model.showCoords) {
                    gfx.draw(_polys[xx][yy]);
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
        // Log.info("renderDirtyItems [rects=" + _dirtyRects.size() +
        // ", items=" + _dirtyItems.size() + "].");

        // sort the dirty sprites and objects visually back-to-front
        DirtyItem items[] = _dirtyItems.sort();

        // render each item clipping to its dirty rectangle
        for (int ii = 0; ii < items.length; ii++) {
            items[ii].paint(gfx, items[ii].dirtyRect);
            // Log.info("Painting item [item=" + items[ii] + "].");
        }
    }

    /**
     * Generates and stores bounding polygons for all object tiles in the
     * scene for later use while rendering.
     */
    protected void initAllObjectBounds ()
    {
        // clear out any previously existing object polygons
        _objpolys.clear();

        // generate bounding polygons for all objects
        ObjectTileLayer tiles = _scene.getObjectLayer();
        for (int yy = 0; yy < tiles.getHeight(); yy++) {
            for (int xx = 0; xx < tiles.getWidth(); xx++) {
                ObjectTile tile = tiles.getTile(xx, yy);
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

        // save it off in the object bounds hashtable
        _objpolys.put(key, newObjectBounds(tile, x, y));
    }

    /**
     * Creates and returns a new polygon bounding the given object
     * tile positioned at the given scene coordinates.
     */
    protected Polygon newObjectBounds (ObjectTile tile, int x, int y)
    {
        return IsoUtil.getObjectBounds(_model, _polys[x][y], tile);
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
                // get the top-left screen coordinates of the tile
                Rectangle bounds = _polys[xx][yy].getBounds();

                // only draw coordinates if the tile is on-screen
                if (bounds.intersects(_model.bounds)) {
                    int sx = bounds.x, sy = bounds.y;

                    // draw x-coordinate
                    String str = String.valueOf(xx);
                    int xpos = sx + cx - (fm.stringWidth(str) / 2);
                    gfx.drawString(str, xpos, sy + cy);

                    // draw y-coordinate
                    str = String.valueOf(yy);
                    xpos = sx + cx - (fm.stringWidth(str) / 2);
                    gfx.drawString(str, xpos, sy + cy + fhei);
                }
            }
        }
    }

//     /**
//      * Paint demarcations at all locations in the scene, with each
//      * location's cluster index, if any, along the right side of its
//      * rectangle.
//      *
//      * @param gfx the graphics context.
//      */
//     protected void paintLocations (Graphics2D gfx)
//     {
// 	List locations = _scene.getLocations();
// 	int size = locations.size();

// 	for (int ii = 0; ii < size; ii++) {
// 	    // retrieve the location
// 	    Location loc = (Location)locations.get(ii);

// 	    // get the cluster index this location is in, if any
// 	    int clusteridx = MisoSceneUtil.getClusterIndex(_scene, loc);

//             // get the location's center coordinate
// 	    Point spos = new Point();
// 	    IsoUtil.fullToScreen(_model, loc.x, loc.y, spos);
// 	    int cx = spos.x, cy = spos.y;

//             // paint the location
//             loc.paint(gfx, cx, cy);

// 	    if (clusteridx != -1) {
// 		// draw the cluster index number on the right side
// 		gfx.setFont(_font);
// 		gfx.setColor(Color.white);
// 		gfx.drawString(String.valueOf(clusteridx), cx + 5, cy + 3);
// 	    }

//             // highlight the location if it's the default entrance
//             if (_scene.getEntrance() == loc) {
//                 gfx.setColor(Color.cyan);
//                 gfx.drawRect(spos.x - 5, spos.y - 5, 10, 10);
//             }
//         }
//     }

    // documentation inherited
    public void invalidateRects (DirtyRectList rects)
    {
        int size = rects.size();
        for (int ii = 0; ii < size; ii++) {
            Rectangle r = (Rectangle)rects.get(ii);

	    // dirty the tiles impacted by this rectangle
	    Rectangle tileBounds = invalidateScreenRect(r);

            // dirty any sprites or objects impacted by this rectangle
            invalidateItems(tileBounds);

	    // save the rectangle for potential display later
	    _dirtyRects.add(r);
        }
    }

    /**
     * Invalidates the given rectangle in screen pixel coordinates in
     * the view.  Returns a rectangle that bounds all tiles that were
     * dirtied.
     *
     * @param rect the dirty rectangle.
     */
    public Rectangle invalidateScreenRect (Rectangle r)
    {
        // initialize the rectangle bounding all tiles dirtied by the
        // invalidated rectangle
        Rectangle tileBounds = new Rectangle(-1, -1, 0, 0);

	// note that corner tiles may be included unnecessarily, but
	// checking to determine whether they're actually needed
	// complicates the code with likely-insufficient benefit

	// determine the top-left tile impacted by this rect
        Point tpos = new Point();
        IsoUtil.screenToTile(_model, r.x, r.y, tpos);

	// determine screen coordinates for top-left tile
	Point topleft = new Point();
	IsoUtil.tileToScreen(_model, tpos.x, tpos.y, topleft);

	// determine number of horizontal and vertical tiles for rect
	int numh = (int)Math.ceil((float)r.width / (float)_model.tilewid);
	int numv = (int)Math.ceil((float)r.height / (float)_model.tilehhei);

	// set up iterating variables
	int tx = tpos.x, ty = tpos.y, mx = tpos.x, my = tpos.y;

	// set the starting screen y-position
	int screenY = topleft.y;

	// add top row if rect may overlap
	if (r.y < (screenY + _model.tilehhei)) {
	    ty--;
	    for (int ii = 0; ii < numh; ii++) {
		addDirtyTile(tileBounds, tx++, ty--);
	    }
	}

	// add rows to the bottom if rect may overlap
	int ypos = screenY + (numv * _model.tilehhei);
	if ((r.y + r.height) > ypos) {
	    numv += ((r.y + r.height) > (ypos + _model.tilehhei)) ? 2 : 1;
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
  	    if (r.x > screenX + _model.tilewid) {
  		tx++;
  		ty--;
		screenX += _model.tilewid;
  	    }

	    // add to the right edge if rect may overlap
	    if (r.x + r.width > (screenX + (length * _model.tilewid))) {
		length++;
	    }

	    // add all tiles in the row to the dirty set
	    for (int jj = 0; jj < length; jj++) {
		addDirtyTile(tileBounds, tx++, ty--);
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

        return tileBounds;
    }

    /**
     * Marks the tile at the given coordinates dirty and expands the
     * tile bounds rectangle to include the rectangle for the dirtied
     * tile.
     */
    protected void addDirtyTile (Rectangle tileBounds, int x, int y)
    {
        if (!_model.isCoordinateValid(x, y)) {
            return;
        }

        // expand the tile bounds rectangle to include this tile
        Rectangle bounds = _polys[x][y].getBounds();
        if (tileBounds.x == -1) {
            tileBounds.setBounds(bounds);
        } else {
            tileBounds.add(bounds);
        }

	// do nothing if the tile's already dirty
	if (_dirty[x][y]) {
	    return;
	}

	// mark the tile dirty
	_numDirty++;
	_dirty[x][y] = true;
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
            ObjectTileLayer tiles = _scene.getObjectLayer();
            Iterator iter = _objpolys.keys();
            while (iter.hasNext()) {
                // get the object's coordinates and bounding polygon
                int coord = ((Integer)iter.next()).intValue();
                Polygon poly = (Polygon)_objpolys.get(coord);

                if (poly.intersects(r)) {
                    // get the dirty portion of the object
                    Rectangle drect = poly.getBounds().intersection(r);
                    int tx = coord >> 16, ty = coord & 0x0000FFFF;
                    _dirtyItems.appendDirtyObject(
                        tiles.getTile(tx, ty), poly, tx, ty, drect);
                    // Log.info("Dirtied item: Object(" + tx + ", " +
                    // ty + ")");
                }
            }
        }
    }

    // documentation inherited
    public Path getPath (MisoCharacterSprite sprite, int x, int y)
    {
        // make sure the destination point is within our bounds
        if (!_model.bounds.contains(x, y)) {
            return null;
        }

        // get the destination tile coordinates
        Point dest = new Point();
        IsoUtil.screenToTile(_model, x, y, dest);

        // get a reasonable tile path through the scene
	List points = AStarPathUtil.getPath(
            _scene.getBaseLayer(), _model.scenewid, _model.scenehei,
            sprite, sprite.getTileX(), sprite.getTileY(), dest.x, dest.y);

	// construct a path object to guide the sprite on its merry way
        return (points == null) ? null :
            new TilePath(_model, sprite, points, x, y);
    }

    /** The stroke used to draw dirty rectangles. */
    protected static final Stroke DIRTY_RECT_STROKE = new BasicStroke(2);

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

    /** The scene to be displayed. */
    protected DisplayMisoScene _scene;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;
}
