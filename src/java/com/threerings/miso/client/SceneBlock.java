//
// $Id: SceneBlock.java,v 1.1 2003/04/17 19:21:16 mdb Exp $

package com.threerings.miso.client;

import java.awt.Polygon;
import java.awt.Rectangle;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;

import com.threerings.miso.Log;
import com.threerings.miso.data.MisoSceneModel;
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
        _bounds = new Rectangle(tx, ty, width, height);
        _base = new BaseTile[width*height];
        _fringe = new Tile[width*height];
        _covered = new boolean[width*height];

        // compute our screen-coordinate footprint polygon
        _footprint = MisoUtil.getFootprintPolygon(
            panel.getSceneMetrics(), tx, ty, width, height);

        // resolve our base tiles
        MisoSceneModel model = panel.getSceneModel();
        TileManager tmgr = panel.getTileManager();
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int x = tx + xx, y = ty + yy;
                int fqTileId = model.getBaseTileId(x, y);
                if (fqTileId <= 0) {
                    continue;
                }

                // this is a bit magical, but the tile manager will fetch
                // tiles from the tileset repository and the tile set id
                // from which we request this tile must map to a base tile
                // as provided by the repository, so we just cast it to a
                // base tile and know that all is well
                String errmsg = null;
                int tidx = yy*width+xx;
                try {
                    _base[tidx] = (BaseTile)tmgr.getTile(fqTileId);
                } catch (ClassCastException cce) {
                    errmsg = "Scene contains non-base tile in base layer";
                } catch (NoSuchTileSetException nste) {
                    errmsg = "Scene contains non-existent tileset";
                } catch (NoSuchTileException nste) {
                    errmsg = "Scene contains non-existent tile";
                }

                if (errmsg != null) {
                    Log.warning(errmsg + " [fqtid=" + fqTileId +
                                ", x=" + x + ", y=" + y + "].");
                }

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
        return _base[index(tx, ty)];
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
     * Returns true if the specified traverser can traverse the specified
     * tile (which is assumed to be in the bounds of this scene block).
     */
    public boolean canTraverse (Object traverser, int tx, int ty)
    {
        BaseTile base = getBaseTile(tx, ty);
        return !_covered[index(tx, ty)] && (base != null && base.isPassable());
    }

    /**
     * Returns the index into our arrays of the specified tile.
     */
    protected final int index (int tx, int ty)
    {
        return (ty-_bounds.y)*_bounds.width + (tx-_bounds.x);
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.toString(_bounds) + ":" + _objects.length;
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
        return ((short)(_bounds.x/_bounds.width+dx) << 16 |
                (short)(_bounds.y/_bounds.height+dy));
    }

    /** Computes the key for the block that holds the specified tile. */
    protected final int blockKey (int tx, int ty)
    {
        return ((short)(tx/_bounds.width) << 16 |
                (short)(ty/_bounds.height));
    }

    /**
     * Sets the footprint of this object tile
     */
    protected void setCovered (HashIntMap blocks, SceneObject scobj)
    {
        int endx = Math.max(0, (scobj.info.x - scobj.tile.getBaseWidth() + 1));
        int endy = Math.max(0, (scobj.info.y - scobj.tile.getBaseHeight() + 1));

        for (int xx = scobj.info.x; xx >= endx; xx--) {
            if ((xx < 0) || (xx >= Short.MAX_VALUE)) {
                continue;
            }

            for (int yy = scobj.info.y; yy >= endy; yy--) {
                if ((yy < 0) || (yy >= Short.MAX_VALUE)) {
                    continue;
                }

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

    /** The bounds of (in tile coordinates) of this block. */
    protected Rectangle _bounds;

    /** A polygon bounding the footprint of this block. */
    protected Polygon _footprint;

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
}
