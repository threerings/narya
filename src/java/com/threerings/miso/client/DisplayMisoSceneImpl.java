//
// $Id: DisplayMisoSceneImpl.java,v 1.64 2003/01/13 22:53:56 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Random;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.ObjectSet;
import com.threerings.miso.tile.AutoFringer;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileLayer;
import com.threerings.miso.tile.MisoTileManager;

/**
 * The default implementation of the {@link DisplayMisoScene} interface.
 */
public class DisplayMisoSceneImpl
    implements DisplayMisoScene
{
    /**
     * Constructs an instance that will be used to display the supplied
     * miso scene data. The tiles identified by the scene model will be
     * loaded via the supplied tile manager.
     *
     * @param model the scene data that we'll be displaying.
     * @param tmgr the tile manager from which to load our tiles.
     */
    public DisplayMisoSceneImpl (MisoSceneModel model, MisoTileManager tmgr)
    {
        this(model);
        setTileManager(tmgr);
    }

    /**
     * Constructs an instance that will be used to display the supplied
     * miso scene data. The tiles identified by the scene model will not
     * be loaded until a tile manager is provided via {@link
     * #setTileManager}.
     *
     * @param model the scene data that we'll be displaying.
     */
    public DisplayMisoSceneImpl (MisoSceneModel model)
    {
        setMisoSceneModel(model);
    }

    /**
     * Instructs this miso scene to use the supplied model instead of its
     * current model. This should be followed up by a called to {@link
     * #setTileManager} or {@link #populateLayers} if a tile manager has
     * already been provided.
     */
    protected void setMisoSceneModel (MisoSceneModel model)
    {
        _model = model;

        int swid = _model.width;
        int shei = _model.height;

        // create the individual tile layer objects
        _base = new BaseTileLayer(new BaseTile[swid*shei], swid, shei);
        _fringe = new TileLayer(new Tile[swid*shei], swid, shei);
        _covered = new boolean[swid*shei];
    }

    /**
     * Provides this display miso scene with a tile manager from which it
     * can load up all of its tiles.
     */
    public void setTileManager (MisoTileManager tmgr)
    {
        _tmgr = tmgr;
        _fringer = tmgr.getAutoFringer();
        populateLayers();
    }

    /**
     * Populates the tile layers with tiles from the tile manager.
     */
    protected void populateLayers ()
    {
        int swid = _base.getWidth();
        int shei = _base.getHeight();

        // if we have a fringer, fill in our fringe
        if (_fringer != null) {
            _fringer.fringe(_model, _fringe, _rando);
        }

        // populate the base and fringe layers
        for (int column = 0; column < shei; column++) {
            for (int row = 0; row < swid; row++) {
                // first do the base layer
                int tsid = _model.getBaseTile(column, row);
                if (tsid > 0) {
                    int tid = (tsid & 0xFFFF);
                    tsid >>= 16;
                    // this is a bit magical, but the tile manager will
                    // fetch tiles from the tileset repository and the
                    // tile set id from which we request this tile must
                    // map to a base tile as provided by the repository,
                    // so we just cast it to a base tile and know that all
                    // is well
                    try {
                        BaseTile mtile = (BaseTile)_tmgr.getTile(tsid, tid);
                        _base.setTile(column, row, mtile);
                    } catch (NoSuchTileSetException nste) {
                        Log.warning("Scene contains non-existent tileset " +
                                    "[tsid=" + tsid + ", tid=" + tid + "].");
                    } catch (NoSuchTileException nste) {
                        Log.warning("Scene contains non-existent tile " +
                                    "[tsid=" + tsid + ", tid=" + tid + "].");
                    }
                }
            }
        }

        // sanity check the object layer info
        int ocount = _model.objectTileIds.length;
        if (ocount % 3 != 0) {
            throw new IllegalArgumentException(
                "model.objectTileIds.length % 3 != 0");
        }

        // create object tile instances for our objects
        for (int ii = 0; ii < ocount; ii += 3) {
            int col = _model.objectTileIds[ii];
            int row = _model.objectTileIds[ii+1];
            int fqTid = _model.objectTileIds[ii+2];
            int tsid = (fqTid >> 16) & 0xFFFF, tid = (fqTid & 0xFFFF);
            try {
                expandObject(col, row, tsid, tid, fqTid, ii/3);
            } catch (NoSuchTileSetException te) {
                Log.warning("Scene contains non-existent object tile " +
                            "[tsid=" + tsid + ", tid=" + tid +
                            ", col=" + col + ", row=" + row + "].");
            }
        }
    }

    /**
     * Called to expand each object read from the model into an actual
     * object tile instance with the appropriate additional data.
     *
     * @return the scene object record for the newly created object tile
     * (which will have been put into all the appropriate lists and
     * tables).
     */
    protected SceneObject expandObject (
        int col, int row, int tsid, int tid, int fqTid, int objidx)
        throws NoSuchTileException, NoSuchTileSetException
    {
        // create and initialize an object info record for this object
        SceneObject scobj = createSceneObject(
            col, row, (ObjectTile)_tmgr.getTile(tsid, tid));

        // assign the object's remaining attributes
        if (!StringUtil.blank(_model.objectActions[objidx])) {
            scobj.action = _model.objectActions[objidx];
        }
        // if we have object priorities, use 'em
        if (_model.objectPrios != null) {
            scobj.priority = _model.objectPrios[objidx];
        }

        // generate a "shadow" for this object tile by toggling the
        // "covered" flag on in all base tiles below it (to prevent
        // sprites from walking on those tiles)
        setObjectTileFootprint(scobj.tile, col, row, true);

        // add the info record to the list
        _objects.add(scobj);

        // return the object info so that derived classes may access it
        return scobj;
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        return _base.getTile(x, y);
    }

    // documentation inherited from interface
    public Tile getFringeTile (int x, int y)
    {
        return _fringe.getTile(x, y);
    }

    // documentation inherited from interface
    public void getSceneObjects (Rectangle region, ObjectSet set)
    {
        // iterate over all of our objects, creating and including scene
        // objects for those that intersect the region
        int ocount = _objects.size();
        for (int ii = 0; ii < ocount; ii++) {
            SceneObject scobj = (SceneObject)_objects.get(ii);
            if (region.contains(scobj.x, scobj.y)) {
                set.insert(scobj);
            }
        }
    }

    // documentation inherited from interface
    public boolean canTraverse (Object trav, int x, int y)
    {
        BaseTile tile = getBaseTile(x, y);
        return (((tile == null) || tile.isPassable()) &&
                !_covered[y*_model.width+x]);
    }

    /**
     * Return a string representation of this Miso scene object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[width=").append(_base.getWidth());
        buf.append(", height=").append(_base.getHeight());
        return buf.append("]").toString();
    }

    /**
     * Creates a scene object record. This allows derived classes to
     * provide extended records.
     */
    protected SceneObject createSceneObject (int x, int y, ObjectTile tile)
    {
        return new SceneObject(x, y, tile);
    }

    /**
     * Locates the scene object record for the object tile at the
     * specified location. Two of the same kind of object tile cannot
     * exist at the same location.
     */
    protected SceneObject getSceneObject (ObjectTile tile, int x, int y)
    {
        int ocount = _objects.size();
        for (int ii = 0; ii < ocount; ii++) {
            SceneObject scobj = (SceneObject)_objects.get(ii);
            if (scobj.tile == tile && scobj.x == x && scobj.y == y) {
                return scobj;
            }
        }
        return null;
    }

    /**
     * Sets the "covered" flag on all base tiles that are in the footprint
     * of the specified object tile.
     *
     * @param otile the object tile whose footprint should be set.
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     * @param covered whether or not the footprint is being covered or
     * uncovered.
     */
    protected void setObjectTileFootprint (
        ObjectTile otile, int x, int y, boolean covered)
    {
        int endx = Math.max(0, (x - otile.getBaseWidth() + 1));
        int endy = Math.max(0, (y - otile.getBaseHeight() + 1));

        for (int xx = x; xx >= endx; xx--) {
            if ((xx < 0) || (xx >= _model.width)) {
                continue;
            }

            for (int yy = y; yy >= endy; yy--) {
                if ((yy < 0) || (yy >= _model.height)) {
                    continue;
                }

                _covered[yy*_model.width+xx] = true;
            }
        }

        // Log.info("Set object tile footprint [tile=" + otile + ", sx=" + x +
        // ", sy=" + y + ", ex=" + endx + ", ey=" + endy + "].");
    }

    /** The tile manager from which we load tiles. */
    protected MisoTileManager _tmgr;

    /** The miso scene model from which we obtain our data. */
    protected MisoSceneModel _model;

    /** The base layer of tiles. */
    protected BaseTileLayer _base;

    /** The fringe layer of tiles. */
    protected TileLayer _fringe;

    /** Information on which tiles are covered by object tiles. */
    protected boolean[] _covered;

    /** The scene object records. */
    protected ArrayList _objects = new ArrayList();

    /** The autofringer. */
    protected AutoFringer _fringer;

    /** A random number generator for filling random base tiles and fringes. */
    protected Random _rando = new Random();
}
