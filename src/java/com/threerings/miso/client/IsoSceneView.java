//
// $Id: IsoSceneView.java,v 1.112 2002/06/18 22:38:12 mdb Exp $

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
import com.samskivert.util.HashIntMap;

import com.threerings.media.RegionManager;

import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.util.Path;

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
     * @param spritemgr the sprite manager.
     * @param model the data model.
     * @param remgr the region manager that is collecting invalid regions
     * for this view.
     */
    public IsoSceneView (SpriteManager spritemgr, IsoSceneViewModel model,
                         RegionManager remgr)
    {
        // save off references
        _spritemgr = spritemgr;
        _model = model;
        _remgr = remgr;

        // handy rectangle
        _tbounds = new Rectangle(0, 0, _model.tilewid, _model.tilehei);
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

        // obtain a list of the objects in the scene and generate records
        // for each of them that contain precomputed metrics
        prepareObjectList();

        // invalidate the entire screen as there's a new scene in town
        invalidate();
    }

    /**
     * Invalidate the entire visible scene view.
     */
    public void invalidate ()
    {
        _remgr.invalidateRegion(_model.bounds);
    }

    // documentation inherited from interface
    public void paint (Graphics2D gfx, Rectangle dirtyRect)
    {
        if (_scene == null) {
            Log.warning("Scene view painted with null scene.");
            return;
        }

        // render any intersecting tiles
        renderTiles(gfx, dirtyRect);

        // render anything that goes on top of the tiles
        renderBaseDecorations(gfx, dirtyRect);

        // render our dirty sprites and objects
        renderDirtyItems(gfx, dirtyRect);

        // draw sprite paths
        if (_model.showPaths) {
            _spritemgr.renderSpritePaths(gfx);
        }

        // paint our highlighted tile (if any)
        paintHighlights(gfx, dirtyRect);

        // paint any extra goodies
        paintExtras(gfx, dirtyRect);
    }

    /**
     * Paints the highlighted tile.
     *
     * @param gfx the graphics context.
     */
    protected void paintHighlights (Graphics2D gfx, Rectangle clip)
    {
        // if we're not highlighting object tiles, bail now
        if (_hmode == HIGHLIGHT_NEVER) {
            return;
        }

        Polygon hpoly = null;

        // if the highlighted object is an object tile, we want to
        // highlight that
        if (_hobject instanceof ObjectTile) {
            ObjectTile otile = (ObjectTile)_hobject;
            // if we're only highlighting objects with actions, make sure
            // this one has an action
            String action = _scene.getObjectAction(otile);
            if (_hmode != HIGHLIGHT_WITH_ACTION || !StringUtil.blank(action)) {
                hpoly = IsoUtil.getObjectFootprint(
                    _model, _hcoords.x, _hcoords.y, otile);
            }
        }

        // if we have no highlight object, but we're in HIGHLIGHT_ALL,
        // then paint the bounds of the highlighted base tile
        if (hpoly == null && _hmode == HIGHLIGHT_ALL &&
            _hcoords.x != -1 && _hcoords.y != -1) {
            hpoly = IsoUtil.getTilePolygon(_model, _hcoords.x, _hcoords.y);
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
    protected void paintExtras (Graphics2D gfx, Rectangle clip)
    {
        // nothing for now
    }

    /**
     * Renders the base and fringe layer tiles that intersect the
     * specified clipping rectangle.
     */
    protected void renderTiles (Graphics2D gfx, Rectangle clip)
    {
        // if we're showing coordinates, we need to do some setting up
        int thw = 0, thh = 0, fhei = 0;
        FontMetrics fm = null;
        if (_model.showCoords) {
            fm = gfx.getFontMetrics(_font);
            fhei = fm.getAscent();
            thw = _model.tilehwid;
            thh = _model.tilehhei;
            gfx.setFont(_font);
            gfx.setColor(Color.white);
        }

        // determine which tiles intersect this clipping region: this is
        // going to be nearly incomprehensible without some sort of
        // diagram; i'll do what i can to comment it, but you'll want to
        // print out a scene diagram (docs/miso/scene.ps) and start making
        // notes if you want to follow along

        // obtain our upper left tile
        Point tpos = IsoUtil.screenToTile(_model, clip.x, clip.y, new Point());

        // determine which quadrant of the upper left tile we occupy
        Point spos = IsoUtil.tileToScreen(_model, tpos.x, tpos.y, new Point());
        boolean left = (clip.x - spos.x < _model.tilehwid);
        boolean top = (clip.y - spos.y < _model.tilehhei);

        // set up our tile position counters
        int dx, dy;
        if (left) {
            dx = 0; dy = 1;
        } else {
            dx = 1; dy = 0;
        }

        // if we're in the top-half of the tile we need to move up a row,
        // either forward or back depending on whether we're in the left
        // or right half of the tile
        if (top) {
            if (left) {
                tpos.x -= 1;
            } else {
                tpos.y -= 1;
            }
            // we'll need to start zig-zagging the other way as well
            dx = 1 - dx;
            dy = 1 - dy;
        }

        // these will bound our loops
        int rightx = clip.x + clip.width, bottomy = clip.y + clip.height;

//         Log.info("Preparing to render [tpos=" + StringUtil.toString(tpos) +
//                  ", left=" + left + ", top=" + top +
//                  ", clip=" + StringUtil.toString(clip) +
//                  ", spos=" + StringUtil.toString(spos) +
//                  "].");

        // obtain the coordinates of the tile that starts the first row
        // and loop through, rendering the intersecting tiles
        IsoUtil.tileToScreen(_model, tpos.x, tpos.y, spos);
        while (spos.y < bottomy) {
            // set up our row counters
            int tx = tpos.x, ty = tpos.y;
            _tbounds.x = spos.x;
            _tbounds.y = spos.y;

//             Log.info("Rendering row [tx=" + tx + ", ty=" + ty + "].");

            // render the tiles in this row
            while (_tbounds.x < rightx) {
                // draw the base and fringe tile images
                try {
                    Tile tile;
                    if ((tile = _scene.getBaseTile(tx, ty)) != null) {
                        tile.paint(gfx, _tbounds.x, _tbounds.y);
                    }
                    if ((tile = _scene.getFringeTile(tx, ty)) != null) {
                        tile.paint(gfx, _tbounds.x, _tbounds.y);
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.warning("Whoops, booched it [tx=" + tx +
                                ", ty=" + ty + ", tb.x=" + _tbounds.x +
                                ", rightx=" + rightx + "].");
                }

                // if we're showing coordinates, do that
                if (_model.showCoords) {
                    // outline the tile
//                     gfx.draw(tpoly);

                    // get the top-left screen coordinates of the tile
                    int sx = _tbounds.x, sy = _tbounds.y;

                    // draw x-coordinate
                    String str = String.valueOf(tx);
                    int xpos = sx + thw - (fm.stringWidth(str) / 2);
                    gfx.drawString(str, xpos, sy + thh);

                    // draw y-coordinate
                    str = String.valueOf(ty);
                    xpos = sx + thw - (fm.stringWidth(str) / 2);
                    gfx.drawString(str, xpos, sy + thh + fhei);
                }

                // move one tile to the right
                tx += 1; ty -= 1;
                _tbounds.x += _model.tilewid;
            }

            // update our tile coordinates
            tpos.x += dx; dx = 1-dx;
            tpos.y += dy; dy = 1-dy;

            // obtain the screen coordinates of the next starting tile
            IsoUtil.tileToScreen(_model, tpos.x, tpos.y, spos);
        }
    }

    /**
     * A function where derived classes can paint things after the base
     * tiles have been rendered but before anything else has been rendered
     * (so that whatever is painted appears to be on the ground).
     */
    protected void renderBaseDecorations (Graphics2D gfx, Rectangle clip)
    {
        // nothing for now
    }

    /**
     * Renders the dirty sprites and objects in the scene to the given
     * graphics context.
     */
    protected void renderDirtyItems (Graphics2D gfx, Rectangle clip)
    {
        // if we don't yet have a scene, do nothing
        if (_scene == null) {
            return;
        }

        // add any sprites impacted by the dirty rectangle
        _dirtySprites.clear();
        _spritemgr.getIntersectingSprites(_dirtySprites, clip);
        int size = _dirtySprites.size();
        for (int ii = 0; ii < size; ii++) {
            MisoCharacterSprite sprite = (MisoCharacterSprite)
                _dirtySprites.get(ii);
            Rectangle bounds = sprite.getBounds();
            if (!bounds.intersects(clip)) {
                continue;
            }

            _dirtyItems.appendDirtySprite(
                sprite, sprite.getTileX(), sprite.getTileY());
            // Log.info("Dirtied item: " + sprite);
        }

        // add any objects impacted by the dirty rectangle
        Iterator iter = _objects.iterator();
        while (iter.hasNext()) {
            ObjectMetrics metrics = (ObjectMetrics)iter.next();
            Rectangle obounds = metrics.bounds;
            if (!obounds.intersects(clip)) {
                continue;
            }

            // compute the footprint if we're rendering those
            Polygon foot = null;
            if (_model.showFootprints) {
                foot = IsoUtil.getObjectFootprint(
                    _model, metrics.x, metrics.y, metrics.tile);
            }

            // add the object to the dirty items list
            _dirtyItems.appendDirtyObject(
                metrics.tile, obounds, foot, metrics.x, metrics.y);
            // Log.info("Dirtied item: Object(" +
            // metrics.x + ", " + metrics.y + ")");
        }

//         Log.info("renderDirtyItems [items=" + _dirtyItems.size() + "].");

        // sort the dirty sprites and objects visually back-to-front;
        // paint them and be done
        _dirtyItems.sort();
        _dirtyItems.paintAndClear(gfx);
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
        Iterator oiter = _scene.getObjectTiles();
        while (oiter.hasNext()) {
            ObjectTile tile = (ObjectTile)oiter.next();
            Point coords = _scene.getObjectCoords(tile);
            generateObjectMetrics(tile, coords.x, coords.y);
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
        metrics.bounds = IsoUtil.getObjectBounds(_model, x, y, tile);

        // and add it to the list
        _objects.add(metrics);
    }

    /**
     * Clears out the object metrics for the specified object.
     */
    protected void clearObjectMetrics (ObjectTile tile)
    {
        int ocount = _objects.size();
        for (int ii = 0; ii < ocount; ii++) {
            ObjectMetrics metrics = (ObjectMetrics)_objects.get(ii);
            if (metrics.tile == tile) {
                _objects.remove(ii);
                return;
            }
        }
    }

    /**
     * Get a path between the two points (in screen coordinates)
     * that will update a MisoCharacterSprite as it walks the path.
     */
    public Path getScreenTilePath (Point start, Point dest, int direction)
    {
        ScreenTilePath path = new ScreenTilePath(_model);
        path.addNode(start.x, start.y, 0); // first direction matters not
        path.addNode(dest.x, dest.y, direction);
        return path;
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
        Point dest = IsoUtil.screenToTile(_model, x, y, new Point());

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
        return IsoUtil.fullToScreen(_model, x, y, new Point());
    }

    // documentation inherited
    public Point getFullCoords (int x, int y)
    {
        return IsoUtil.screenToFull(_model, x, y, new Point());
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
        Object hobject = null;

        // start with the sprites that contain the point
        _spritemgr.getHitSprites(_hitSprites, x, y);
        int hslen = _hitSprites.size();
        for (int i = 0; i < hslen; i++) {
            MisoCharacterSprite sprite =
                (MisoCharacterSprite)_hitSprites.get(i);
            _hitList.appendDirtySprite(
                sprite, sprite.getTileX(), sprite.getTileY());
        }

        // add the object tiles that contain the point
        getHitObjects(_hitList, x, y);

        // sort the list of hit items by rendering order
        _hitList.sort();

        // the last element in the array is what we want (assuming there
        // are any items in the array)
        int icount = _hitList.size();
        if (icount > 0) {
            DirtyItem item = (DirtyItem)_hitList.get(icount-1);
            hobject = item.obj;

            // if this is an object tile, we need to update the hcoords
            if (hobject instanceof ObjectTile) {
                _hcoords.x = item.ox;
                _hcoords.y = item.oy;
            }
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
                    (_scene.getObjectAction((ObjectTile)_hobject) != null) :
                    true;
            }
        }

        // clear out the hitlists
        _hitList.clear();
        _hitSprites.clear();

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
            Rectangle pbounds = metrics.bounds;
            // skip polys that don't contain the point
            if (!pbounds.contains(x, y)) {
                continue;
            }

            // now check that the pixel in the tile image is
            // non-transparent at that point
            if (!metrics.tile.hitTest(x - pbounds.x, y - pbounds.y)) {
                continue;
            }

            // we've passed the test, add the object to the list
            list.appendDirtyObject(metrics.tile, metrics.bounds, null,
                                   metrics.x, metrics.y);
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
        Point npos = IsoUtil.screenToTile(_model, sx, sy, new Point());

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

        /** The object's bounding rectangle. */
        public Rectangle bounds;
    }

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The scene view model data. */
    protected IsoSceneViewModel _model;

    /** Our region manager. */
    protected RegionManager _remgr;

    /** The scene to be displayed. */
    protected DisplayMisoScene _scene;

    /** Metric information for all of the object tiles. */
    protected ArrayList _objects = new ArrayList();

    /** The dirty sprites and objects that need to be re-painted. */
    protected DirtyItemList _dirtyItems = new DirtyItemList();

    /** The working sprites list used when calculating dirty regions. */
    protected ArrayList _dirtySprites = new ArrayList();

    /** Used when rendering tiles. */
    protected Rectangle _tbounds;

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
