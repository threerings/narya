//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.client;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.geom.GeomUtil;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;
import com.threerings.media.util.MathUtil;

import com.threerings.miso.Log;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.util.MisoUtil;
import com.threerings.miso.util.ObjectSet;

/**
 * Contains the base and object tile information on a particular
 * rectangular region of a scene.
 */
public class SceneBlock
{
    /**
     * Creates a scene block and resolves the base and object tiles that
     * reside therein.
     */
    public SceneBlock (
        MisoScenePanel panel, int tx, int ty, int width, int height)
    {
        _panel = panel;
        _bounds = new Rectangle(tx, ty, width, height);
        _base = new BaseTile[width*height];
        _fringe = new BaseTile[width*height];
        _covered = new boolean[width*height];

        // compute our screen-coordinate footprint polygon
        _footprint = MisoUtil.getFootprintPolygon(
            panel.getSceneMetrics(), tx, ty, width, height);

        // the rest of our resolution will happen in resolve()
    }

    /**
     * Makes a note that this block was considered to be visible at the
     * time it was created. This is purely for debugging purposes.
     */
    public void setVisiBlock (boolean visi)
    {
        _visi = visi;
    }

    /**
     * This method is called by the {@link SceneBlockResolver} on the
     * block resolution thread to allow us to load up our image data
     * without blocking the AWT thread.
     */
    protected boolean resolve ()
    {
        // if we got canned before we were resolved, go ahead and bail now
        if (_panel.getBlock(_bounds.x, _bounds.y) != this) {
            // Log.info("Not resolving abandoned block " + this + ".");
            _panel.blockAbandoned(this);
            return false;
        }
        _panel.blockResolving(this);

        // start with the bounds of the footprint polygon
        Rectangle sbounds = new Rectangle(_footprint.getBounds());
        Rectangle obounds = null;

        // resolve our base tiles
        long now = System.currentTimeMillis();
        int baseCount = 0, fringeCount = 0;
        MisoSceneModel model = _panel.getSceneModel();
        for (int yy = 0; yy < _bounds.height; yy++) {
            for (int xx = 0; xx < _bounds.width; xx++) {
                int x = _bounds.x + xx, y = _bounds.y + yy;
                int fqTileId = model.getBaseTileId(x, y);
                if (fqTileId <= 0) {
                    continue;
                }

                // load up this base tile
                updateBaseTile(fqTileId, x, y);
                baseCount++;

                // if there's no tile here, we don't need no fringe
                int tidx = index(x, y);
                if (_base[tidx] == null) {
                    continue;
                }

                // compute the fringe for this tile
                _fringe[tidx] = _panel.computeFringeTile(x, y);
                fringeCount++;
            }
        }

        // DEBUG: check for long resolution times
        long stamp = System.currentTimeMillis();
        long elapsed = stamp - now;
        if (elapsed > 500L) {
            Log.warning("Base and fringe resolution took long time " +
                        "[block=" + this + ", baseCount=" + baseCount +
                        ", fringeCount=" + fringeCount +
                        ", elapsed=" + elapsed + "].");
        }

        // resolve our objects
        ObjectSet set = new ObjectSet();
        model.getObjects(_bounds, set);
        ArrayList scobjs = new ArrayList();
        now = System.currentTimeMillis();
        for (int ii = 0, ll = set.size(); ii < ll; ii++) {
            SceneObject scobj = new SceneObject(_panel, set.get(ii));
            // ignore this object if it failed to resolve
            if (scobj.bounds == null) {
                continue;
            }
            sbounds.add(scobj.bounds);
            obounds = GeomUtil.grow(obounds, scobj.bounds);
            scobjs.add(scobj);

            // DEBUG: check for long resolution times
            stamp = System.currentTimeMillis();
            elapsed = stamp - now;
            now = stamp;
            if (elapsed > 250L) {
                Log.warning("Scene object took look time to resolve " +
                            "[block=" + this + ", scobj=" + scobj +
                            ", elapsed=" + elapsed + "].");
            }
        }
        _objects = (SceneObject[])scobjs.toArray(
            new SceneObject[scobjs.size()]);

        // resolve our default tileset
        int bsetid = model.getDefaultBaseTileSet();
        try {
            if (bsetid > 0) {
                _defset = _panel.getTileManager().getTileSet(bsetid);
            }
        } catch (Exception e) {
            Log.warning("Unable to fetch default base tileset [tsid=" + bsetid +
                        ", error=" + e + "].");
        }

        // this both marks us as resolved and makes all our other updated
        // fields visible
        synchronized (this) {
            _obounds = obounds;
            _sbounds = sbounds;
        }

        return true;
    }

    /**
     * This is called by the {@link SceneBlockResolver} on the AWT thread
     * when our resolution has completed. We inform our containing panel.
     */
    protected void wasResolved ()
    {
        _panel.blockResolved(this);
    }

    /**
     * Returns true if this block has been resolved, false if not.
     */
    protected synchronized boolean isResolved ()
    {
        return _sbounds != null;
    }

    /**
     * Returns the bounds of this block, in tile coordinates.
     */
    public Rectangle getBounds ()
    {
        return _bounds;
    }

    /**
     * Returns the bounds of the screen coordinate rectangle that contains
     * all pixels that are drawn on by all tiles and objects in this
     * block.
     */
    public Rectangle getScreenBounds ()
    {
        return _sbounds;
    }

    /**
     * Returns the bounds of the screen coordinate rectangle that contains
     * all pixels that are drawn on by all objects (but not base tiles) in
     * this block. <em>Note:</em> this will return <code>null</code> if
     * the block has no objects.
     */
    public Rectangle getObjectBounds ()
    {
        return _obounds;
    }

    /**
     * Returns the screen-coordinate polygon bounding the footprint of
     * this block.
     */
    public Polygon getFootprint ()
    {
        return _footprint;
    }

    /**
     * Returns an array of all resolved scene objects in this block.
     */
    public SceneObject[] getObjects ()
    {
        return _objects;
    }

    /**
     * Returns the base tile at the specified coordinates or null if
     * there's no tile at said coordinates.
     */
    public BaseTile getBaseTile (int tx, int ty)
    {
        BaseTile tile = _base[index(tx, ty)];
        if (tile == null && _defset != null) {
            tile = (BaseTile)_defset.getTile(
                TileUtil.getTileHash(tx, ty) % _defset.getTileCount());
        }
        return tile;
    }

    /**
     * Returns the fringe tile at the specified coordinates or null if
     * there's no tile at said coordinates.
     */
    public BaseTile getFringeTile (int tx, int ty)
    {
        return _fringe[index(tx, ty)];
    }

    /**
     * Informs this scene block that the specified base tile has been
     * changed.
     */
    public void updateBaseTile (int fqTileId, int tx, int ty)
    {
        String errmsg = null;
        int tidx = index(tx, ty);

        // this is a bit magical: we pass the fully qualified tile id to
        // the tile manager which loads up from the configured tileset
        // repository the appropriate tileset (which should be a
        // BaseTileSet) and then extracts the appropriate base tile (the
        // index of which is also in the fqTileId)
        try {
            if (fqTileId <= 0) {
                _base[tidx] = null;
            } else {
                _base[tidx] = (BaseTile)
                    _panel.getTileManager().getTile(fqTileId);
            }
            // clear out the fringe (it must be recomputed by the caller)
            _fringe[tidx] = null;

        } catch (ClassCastException cce) {
            errmsg = "Scene contains non-base tile in base layer";
        } catch (NoSuchTileSetException nste) {
            errmsg = "Scene contains non-existent tileset";
        }

        if (errmsg != null) {
            Log.warning(errmsg + " [fqtid=" + fqTileId +
                        ", x=" + tx + ", y=" + ty + "].");
        }
    }

    /**
     * Instructs this block to recompute its fringe at the specified
     * location.
     */
    public void updateFringe (int tx, int ty)
    {
        int tidx = index(tx, ty);
        if (_base[tidx] != null) {
            _fringe[tidx] = _panel.computeFringeTile(tx, ty);
        }
    }

    /**
     * Adds the supplied object to this block. Coverage is not computed
     * for the added object, a subsequent call to {@link #update} will be
     * needed.
     *
     * @return true if the object was added, false if it was not because
     * another object of the same type already occupies that location.
     */
    public boolean addObject (ObjectInfo info)
    {
        // make sure we don't already have this same object at these
        // coordinates
        for (int ii = 0; ii < _objects.length; ii++) {
            if (_objects[ii].info.equals(info)) {
                return false;
            }
        }

        _objects = (SceneObject[])
            ArrayUtil.append(_objects, new SceneObject(_panel, info));

        // clear out our neighbors array so that the subsequent update
        // causes us to recompute our coverage
        Arrays.fill(_neighbors, null);
        return true;
    }

    /**
     * Removes the specified object from this block. Coverage is not
     * recomputed, so a subsequent call to {@link #update} will be needed.
     *
     * @return true if the object was deleted, false if it was not found
     * in our object list.
     */
    public boolean deleteObject (ObjectInfo info)
    {
        int oidx = -1;
        for (int ii = 0; ii < _objects.length; ii++) {
            if (_objects[ii].info.equals(info)) {
                oidx = ii;
                break;
            }
        }
        if (oidx == -1) {
            return false;
        }
        _objects = (SceneObject[])ArrayUtil.splice(_objects, oidx, 1);

        // clear out our neighbors array so that the subsequent update
        // causes us to recompute our coverage
        Arrays.fill(_neighbors, null);
        return true;
    }

    /**
     * Returns true if the specified traverser can traverse the specified
     * tile (which is assumed to be in the bounds of this scene block).
     */
    public boolean canTraverse (Object traverser, int tx, int ty)
    {
        if (_covered[index(tx, ty)]) {
            return false;
        }

        // null base or impassable base kills traversal
        BaseTile base = getBaseTile(tx, ty);
        if ((base == null) || !base.isPassable()) {
            return false;
        }

        // fringe can only kill traversal if it is present
        BaseTile fringe = getFringeTile(tx, ty);
        return (fringe == null) || fringe.isPassable();
    }

    /**
     * Computes the memory usage of the base and object tiles in this
     * scene block; registering counted tiles in the hash map so that
     * other blocks can be sure not to double count them. Base tile usage
     * is placed into the zeroth array element, fringe tile usage into the
     * first and object tile usage into the second.
     */
    public void computeMemoryUsage (
        HashMap bases, HashSet fringes, HashMap objects, long[] usage)
    {
        // account for our base tiles
        MisoSceneModel model = _panel.getSceneModel();
        for (int yy = 0; yy < _bounds.height; yy++) {
            for (int xx = 0; xx < _bounds.width; xx++) {
                int x = _bounds.x + xx, y = _bounds.y + yy;
                int tidx = index(x, y);
                BaseTile base = _base[tidx];
                if (base == null) {
                    continue;
                }

                BaseTile sbase = (BaseTile)bases.get(base.key);
                if (sbase == null) {
                    bases.put(base.key, base);
                    usage[0] += base.getEstimatedMemoryUsage();
                } else if (base != _base[tidx]) {
                    Log.warning("Multiple instances of same base tile " +
                                "[base=" + base +
                                ", x=" + xx + ", y=" + yy + "].");
                    usage[0] += base.getEstimatedMemoryUsage();
                }

                // now account for the fringe
                if (_fringe[tidx] == null) {
                    continue;
                } else if (!fringes.contains(_fringe[tidx])) {
                    fringes.add(_fringe[tidx]);
                    usage[1] += _fringe[tidx].getEstimatedMemoryUsage();
                }
            }
        }

        // now get the object tiles
        int ocount = (_objects == null) ? 0 : _objects.length;
        for (int ii = 0; ii < ocount; ii++) {
            SceneObject scobj = _objects[ii];
            ObjectTile tile = (ObjectTile)objects.get(scobj.tile.key);
            if (tile == null) {
                objects.put(scobj.tile.key, scobj.tile);
                usage[2] += scobj.tile.getEstimatedMemoryUsage();
            } else if (tile != scobj.tile) {
                Log.warning("Multiple instances of same object tile: " +
                            scobj.info + ".");
                usage[2] += scobj.tile.getEstimatedMemoryUsage();
            }
        }
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        int bx = MathUtil.floorDiv(_bounds.x, _bounds.width);
        int by = MathUtil.floorDiv(_bounds.y, _bounds.height);
        return StringUtil.coordsToString(bx, by) + ":" +
            StringUtil.toString(_bounds) + ":" +
            ((_objects == null) ? 0 : _objects.length) +
            (_visi ? ":v" : ":i");
    }

    /**
     * Returns the index into our arrays of the specified tile.
     */
    protected final int index (int tx, int ty)
    {
//         if (!_bounds.contains(tx, ty)) {
//             String errmsg = "Coordinates out of bounds: +" + tx + "+" + ty +
//                 " not in " + StringUtil.toString(_bounds);
//             throw new IllegalArgumentException(errmsg);
//         }
        return (ty-_bounds.y)*_bounds.width + (tx-_bounds.x);
    }

    /**
     * Links this block to its neighbors; informs neighboring blocks of
     * object coverage.
     */
    protected void update (HashIntMap blocks)
    {
        boolean recover = false;

        // link up to our neighbors
        for (int ii = 0; ii < DX.length; ii++) {
            SceneBlock neigh = (SceneBlock)
                blocks.get(neighborKey(DX[ii], DY[ii]));
            if (neigh != _neighbors[ii]) {
                _neighbors[ii] = neigh;
                // if we're linking up to a neighbor for the first time;
                // we need to recalculate our coverage
                recover = recover || (neigh != null);
//                 Log.info(this + " was introduced to " + neigh + ".");
            }
        }

        // if we need to regenerate the set of tiles covered by our
        // objects, do so
        if (recover) {
            for (int ii = 0; ii < _objects.length; ii++) {
                setCovered(blocks, _objects[ii]);
            }
        }
    }

    /** Computes the key of our neighbor. */
    protected final int neighborKey (int dx, int dy)
    {
        int nx = MathUtil.floorDiv(_bounds.x, _bounds.width)+dx;
        int ny = MathUtil.floorDiv(_bounds.y, _bounds.height)+dy;
        return MisoScenePanel.compose(nx, ny);
    }

    /** Computes the key for the block that holds the specified tile. */
    protected final int blockKey (int tx, int ty)
    {
        int bx = MathUtil.floorDiv(tx, _bounds.width);
        int by = MathUtil.floorDiv(ty, _bounds.height);
        return MisoScenePanel.compose(bx, by);
    }

    /**
     * Sets the footprint of this object tile
     */
    protected void setCovered (HashIntMap blocks, SceneObject scobj)
    {
        int endx = scobj.info.x - scobj.tile.getBaseWidth() + 1;
        int endy = scobj.info.y - scobj.tile.getBaseHeight() + 1;

        for (int xx = scobj.info.x; xx >= endx; xx--) {
            for (int yy = scobj.info.y; yy >= endy; yy--) {
                SceneBlock block = (SceneBlock)blocks.get(blockKey(xx, yy));
                if (block != null) {
                    block.setCovered(xx, yy);
                }
            }
        }

//         Log.info("Updated coverage " + scobj.info + ".");
    }

    /**
     * Indicates that this tile is covered by an object footprint.
     */
    protected void setCovered (int tx, int ty)
    {
        _covered[index(tx, ty)] = true;
    }

    /** The panel for which we contain a block. */
    protected MisoScenePanel _panel;

    /** The bounds of (in tile coordinates) of this block. */
    protected Rectangle _bounds;

    /** The bounds (in screen coords) of all images rendered by this block. */
    protected Rectangle _sbounds;

    /** The bounds (in screen coords) of all objects rendered by this block. */
    protected Rectangle _obounds;

    /** A polygon bounding the footprint of this block. */
    protected Polygon _footprint;

    /** Used to return a tile where we have none. */
    protected TileSet _defset;

    /** Our base tiles. */
    protected BaseTile[] _base;

    /** Our fringe tiles. */
    protected BaseTile[] _fringe;

    /** Indicates whether our tiles are covered by an object. */
    protected boolean[] _covered;

    /** Info on our objects. */
    protected SceneObject[] _objects;

    /** Our neighbors in the eight cardinal directions. */
    protected SceneBlock[] _neighbors = new SceneBlock[DX.length];

    /** A debug flag indicating whether we were visible at creation. */
    protected boolean _visi;

    // used to link up to our neighbors
    protected static final int[] DX = { -1, -1,  0,  1, 1, 1, 0, -1 };
    protected static final int[] DY = {  0, -1, -1, -1, 0, 1, 1,  1 };
}
