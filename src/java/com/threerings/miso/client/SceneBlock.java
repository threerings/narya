//
// $Id: SceneBlock.java,v 1.7 2003/04/23 00:37:46 mdb Exp $

package com.threerings.miso.client;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Arrays;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
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
        _fringe = new Tile[width*height];
        _covered = new boolean[width*height];

        // compute our screen-coordinate footprint polygon
        _footprint = MisoUtil.getFootprintPolygon(
            panel.getSceneMetrics(), tx, ty, width, height);

        // resolve our base tiles
        MisoSceneModel model = panel.getSceneModel();
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int x = tx + xx, y = ty + yy;
                int fqTileId = model.getBaseTileId(x, y);
                if (fqTileId <= 0) {
                    continue;
                }

                updateBaseTile(fqTileId, x, y);

                // if there's no tile here, we don't need no fringe
                int tidx = index(x, y);
                if (_base[tidx] == null) {
                    continue;
                }

                // compute the fringe for this tile
                _fringe[tidx] = panel.computeFringeTile(x, y);
            }
        }

        // resolve our objects
        ObjectSet set = new ObjectSet();
        model.getObjects(_bounds, set);
        _objects = new SceneObject[set.size()];
        for (int ii = 0; ii < _objects.length; ii++) {
            _objects[ii] = new SceneObject(panel, set.get(ii));
        }

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
    }

    /**
     * Returns the bounds of this block, in tile coordinates.
     */
    public Rectangle getBounds ()
    {
        return _bounds;
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
            long seed = ((tx^ty) ^ multiplier) & mask;
            long hash = (seed * multiplier + addend) & mask;
            int tidx = (int)((hash >> 10) % _defset.getTileCount());
            tile = (BaseTile)_defset.getTile(tidx);
        }
        return tile;
    }

    /**
     * Returns the fringe tile at the specified coordinates or null if
     * there's no tile at said coordinates.
     */
    public Tile getFringeTile (int tx, int ty)
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

        // this is a bit magical: we pass the fully qualified tile id to
        // the tile manager which loads up from the configured tileset
        // repository the appropriate tileset (which should be a
        // BaseTileSet) and then extracts the appropriate base tile (the
        // index of which is also in the fqTileId)
        try {
            _base[index(tx, ty)] = (BaseTile)
                _panel.getTileManager().getTile(fqTileId);
        } catch (ClassCastException cce) {
            errmsg = "Scene contains non-base tile in base layer";
        } catch (NoSuchTileSetException nste) {
            errmsg = "Scene contains non-existent tileset";
        } catch (NoSuchTileException nste) {
            errmsg = "Scene contains non-existent tile";
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
        BaseTile base = getBaseTile(tx, ty);
        return !_covered[index(tx, ty)] && (base != null && base.isPassable());
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.toString(_bounds) + ":" + _objects.length;
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
        return ((short)(MathUtil.floorDiv(_bounds.x, _bounds.width)+dx) << 16 |
                (short)(MathUtil.floorDiv(_bounds.y, _bounds.height)+dy));
    }

    /** Computes the key for the block that holds the specified tile. */
    protected final int blockKey (int tx, int ty)
    {
        return ((short)MathUtil.floorDiv(tx, _bounds.width) << 16 |
                (short)MathUtil.floorDiv(ty, _bounds.height));
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

    /** A polygon bounding the footprint of this block. */
    protected Polygon _footprint;

    /** Used to return a tile where we have none. */
    protected TileSet _defset;

    /** Our base tiles. */
    protected BaseTile[] _base;

    /** Our fringe tiles. */
    protected Tile[] _fringe;

    /** Indicates whether our tiles are covered by an object. */
    protected boolean[] _covered;

    /** Info on our objects. */
    protected SceneObject[] _objects;

    /** Our neighbors in the eight cardinal directions. */
    protected SceneBlock[] _neighbors = new SceneBlock[DX.length];

    // used to link up to our neighbors
    protected static final int[] DX = { -1, -1,  0,  1, 1, 1, 0, -1 };
    protected static final int[] DY = {  0, -1, -1, -1, 0, 1, 1,  1 };

    // for mapping tile coordinates to a pseudo-random tile
    protected final static long multiplier = 0x5DEECE66DL;
    protected final static long addend = 0xBL;
    protected final static long mask = (1L << 48) - 1;
}
