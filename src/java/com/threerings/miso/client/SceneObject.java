//
// $Id: SceneObject.java,v 1.10 2004/01/11 08:24:16 mdb Exp $

package com.threerings.miso.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.StringUtil;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.Log;
import com.threerings.miso.MisoPrefs;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;

/**
 * Contains resolved information on an object in a scene.
 */
public class SceneObject
{
    /** The object's info record. */
    public ObjectInfo info;

    /** The object tile used to display this object. */
    public ObjectTile tile;

    /** The screen coordinate bounds of our object tile given its position
     * in the scene. */
    public Rectangle bounds;

    /**
     * Creates a scene object for display by the specified panel. The
     * appropriate object tile is resolved and the object's in-situ bounds
     * are computed.
     */
    public SceneObject (MisoScenePanel panel, ObjectInfo info)
    {
        this.info = info;

        // resolve our object tile
        refreshObjectTile(panel);
    }

    /**
     * Creates a scene object for display by the specified panel.
     */
    public SceneObject (MisoScenePanel panel, ObjectInfo info, ObjectTile tile)
    {
        this(panel.getSceneMetrics(), info, tile);
    }

    /**
     * Creates a scene object for display according to the supplied metrics.
     */
    public SceneObject (
        MisoSceneMetrics metrics, ObjectInfo info, ObjectTile tile)
    {
        this.info = info;
        this.tile = tile;
        computeInfo(metrics);
    }

    /**
     * Used to flag overlapping scene objects that have no resolving
     * object priorities.
     */
    public void setWarning (boolean warning)
    {
        _warning = warning;
    }

    /**
     * Requests that this scene object render itself.
     */
    public void paint (Graphics2D gfx)
    {
        if (_hideObjects.getValue()) {
            return;
        }

        // if we're rendering footprints, paint that
        boolean footpaint = _fprintDebug.getValue();
        if (footpaint) {
            gfx.setColor(Color.black);
            gfx.draw(_footprint);
        }

        // if we have a warning, render an alpha'd red rectangle over our
        // bounds
        if (_warning) {
            Composite ocomp = gfx.getComposite();
            gfx.setComposite(ALPHA_WARN);
            gfx.fill(bounds);
            gfx.setComposite(ocomp);
        }

        // paint our tile
        tile.paint(gfx, bounds.x, bounds.y);

        // and possibly paint the object's spot
        if (footpaint && _sspot != null) {
            // translate the origin to center on the portal
            gfx.translate(_sspot.x, _sspot.y);

            // rotate to reflect the spot orientation
            double rot = (Math.PI / 4.0f) * tile.getSpotOrient();
            gfx.rotate(rot);

            // draw the spot triangle
            gfx.setColor(Color.green);
            gfx.fill(_spotTri);

            // outline the triangle in black
            gfx.setColor(Color.black);
            gfx.draw(_spotTri);

            // restore the original transform
            gfx.rotate(-rot);
            gfx.translate(-_sspot.x, -_sspot.y);
        }
    }

    /**
     * Returns the location associated with this object's "spot" in fine
     * coordinates or null if it has no spot.
     */
    public Point getObjectSpot ()
    {
        return _fspot;
    }

    /**
     * Returns the location associated with this object's "spot" in screen
     * coordinates or null if it has no spot.
     */
    public Point getObjectScreenSpot ()
    {
        return _sspot;
    }

    /**
     * Returns true if this object's footprint overlaps that of the
     * specified other object.
     */
    public boolean objectFootprintOverlaps (SceneObject so)
    {
        return _frect.intersects(so._frect);
    }

    /**
     * Returns true if this object's footprint overlaps the supplied tile
     * coordinate rectangle.
     */
    public boolean objectFootprintOverlaps (Rectangle rect)
    {
        return _frect.intersects(rect);
    }

    /**
     * Returns a polygon bounding all footprint tiles of this scene
     * object.
     *
     * @return the bounding polygon.
     */
    public Polygon getObjectFootprint ()
    {
        return _footprint;
    }

    /**
     * Returns the render priority of this scene object.
     */
    public int getPriority ()
    {
        // if we have no overridden priority, return our object tile's
        // default priority
        return (info.priority == 0 && tile != null) ?
            tile.getPriority() : info.priority;
    }

    /**
     * Overrides the render priority of this object.
     */
    public void setPriority (byte priority)
    {
        info.priority = (byte)Math.max(
            Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, priority));
    }

    /**
     * Informs this scene object that the mouse is now hovering over it.
     * Custom objects may wish to adjust some internal state and return
     * true from this method indicating that they should be repainted.
     */
    public boolean setHovered (boolean hovered)
    {
        return false;
    }

    /**
     * Updates this object's origin tile coordinate. It's bounds and other
     * cached screen coordinate information are updated.
     */
    public void relocateObject (MisoSceneMetrics metrics, int tx, int ty)
    {
//         Log.info("Relocating object " + this + " to " +
//                  StringUtil.coordsToString(tx, ty));
        info.x = tx;
        info.y = ty;
        computeInfo(metrics);
    }

    /**
     * Reloads and recolorizes our object tile. It is not intended for the
     * actual object tile used by a scene object to change in its
     * lifetime, only attributes of that object like its colorizations. So
     * don't do anything crazy like change our {@link ObjectInfo}'s
     * <code>tileId</code> and call this method or things might break.
     */
    public void refreshObjectTile (MisoScenePanel panel)
    {
        int tsid = TileUtil.getTileSetId(info.tileId);
        int tidx = TileUtil.getTileIndex(info.tileId);
        try {
            tile = (ObjectTile)panel.getTileManager().getTile(
                tsid, tidx, panel.getColorizer(info));
            computeInfo(panel.getSceneMetrics());

        } catch (NoSuchTileException nste) {
            Log.warning("Scene contains non-existent object tile " +
                        "[info=" + info + "].");
        } catch (NoSuchTileSetException te) {
            Log.warning("Scene contains non-existent object tileset " +
                        "[info=" + info + "].");
        }
    }

    /**
     * Computes our screen bounds, tile footprint and other useful cached
     * metrics.
     */
    protected void computeInfo (MisoSceneMetrics metrics)
    {
        // start with the screen coordinates of our origin tile
        Point tpos = MisoUtil.tileToScreen(
            metrics, info.x, info.y, new Point());

        // if the tile has an origin coordinate, use that, otherwise
        // compute it from the tile footprint
        int tox = tile.getOriginX(), toy = tile.getOriginY();
        if (tox == Integer.MIN_VALUE) {
            tox = tile.getBaseWidth() * metrics.tilehwid;
        }
        if (toy == Integer.MIN_VALUE) {
            toy = tile.getHeight();
        }

        bounds = new Rectangle(tpos.x + metrics.tilehwid - tox,
                               tpos.y + metrics.tilehei - toy,
                               tile.getWidth(), tile.getHeight());

        // compute our object footprint as well
        _frect = new Rectangle(info.x - tile.getBaseWidth() + 1,
                               info.y - tile.getBaseHeight() + 1,
                               tile.getBaseWidth(), tile.getBaseHeight());
        _footprint = MisoUtil.getFootprintPolygon(
            metrics, _frect.x, _frect.y, _frect.width, _frect.height);

        // compute our object spot if we've got one
        if (tile.hasSpot()) {
            _fspot = MisoUtil.tilePlusFineToFull(
                metrics, info.x, info.y, tile.getSpotX(), tile.getSpotY(),
                new Point());
            _sspot = MisoUtil.fullToScreen(
                metrics, _fspot.x, _fspot.y, new Point());
        }

//         Log.info("Computed object metrics " +
//                  "[tpos=" + StringUtil.coordsToString(tx, ty) +
//                  ", info=" + info +
//                  ", sbounds=" + StringUtil.toString(bounds) + "].");
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return info + "[" + StringUtil.toString(bounds) + "]";
    }

    /** Our object as a tile coordinate rectangle. */
    protected Rectangle _frect;

    /** Our object footprint as a polygon. */
    protected Polygon _footprint;

    /** The full-coordinates of our object spot; or null if we have none. */
    protected Point _fspot;

    /** The screen-coordinates of our object spot; or null if we have none. */
    protected Point _sspot;

    /** Used to mark objects with errors. */
    protected boolean _warning;

    /** A debug hook that toggles rendering of objects. */
    protected static RuntimeAdjust.BooleanAdjust _hideObjects =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles rendering of objects in the scene view.",
            "narya.miso.hide_objects", MisoPrefs.config, false);

    /** A debug hook that toggles rendering of object footprints. */
    protected static RuntimeAdjust.BooleanAdjust _fprintDebug =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles rendering of object footprints in the scene view.",
            "narya.miso.iso_fprint_debug_render", MisoPrefs.config, false);

    /** The triangle used to render an object's spot. */
    protected static Polygon _spotTri;

    static {
        _spotTri = new Polygon();
        _spotTri.addPoint(-3, -3);
        _spotTri.addPoint(3, -3);
        _spotTri.addPoint(0, 3);
    };

    /** The alpha used to fill our bounds for warning purposes. */
    protected static final Composite ALPHA_WARN =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
}
