//
// $Id: DisplayMisoSceneImpl.java,v 1.66 2003/02/04 21:39:28 mdb Exp $

package com.threerings.miso.client;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Random;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;

import com.threerings.miso.Log;
import com.threerings.miso.client.util.ObjectSet;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;
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
     * Provides this display miso scene with a tile manager from which it
     * can load up all of its tiles.
     */
    public void setTileManager (MisoTileManager tmgr)
    {
        _tmgr = tmgr;
        _fringer = tmgr.getAutoFringer();
        populateLayers();
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
    public void getObjects (Rectangle region, ObjectSet set)
    {
        // iterate over all of our objects, creating and including scene
        // objects for those that intersect the region
        int ocount = _objects.size();
        for (int ii = 0; ii < ocount; ii++) {
            DisplayObjectInfo scobj = (DisplayObjectInfo)_objects.get(ii);
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

        // create display object infos for our uninteresting objects
        int ocount = (_model.objectTileIds == null) ? 0 :
            _model.objectTileIds.length;
        for (int ii = 0; ii < ocount; ii++) {
            _objects.add(new DisplayObjectInfo(_model.objectTileIds[ii],
                                               _model.objectXs[ii],
                                               _model.objectYs[ii]));
        }

        // create display object infos for our interesting objects
        for (int ii = 0, ll = _model.objectInfo.length; ii < ll; ii++) {
            _objects.add(new DisplayObjectInfo(_model.objectInfo[ii]));
        }
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

        // populate our display objects with object tiles
        for (int ii = 0; ii < _objects.size(); ii++) {
            if (!populateObject((DisplayObjectInfo)_objects.get(ii))) {
                // initialization failed, remove the object
                _objects.remove(ii--);
            }
        }
    }

    /**
     * Called to populate an object with its object tile when we have been
     * configured with a tile manager from which to obtain such things.
     */
    protected boolean populateObject (DisplayObjectInfo info)
    {
        int tsid = (info.tileId >> 16) & 0xFFFF, tid = (info.tileId & 0xFFFF);
        try {
            initObject(info, (ObjectTile)_tmgr.getTile(tsid, tid));
            return true;
        } catch (NoSuchTileException nste) {
            Log.warning("Scene contains non-existent object tile " +
                        "[info=" + info + ", tsid=" + tsid +
                        ", tid=" + tid + "].");
        } catch (NoSuchTileSetException te) {
            Log.warning("Scene contains non-existent object tile " +
                        "[info=" + info + ", tsid=" + tsid +
                        ", tid=" + tid + "].");
        }
        return false;
    }

    /**
     * Initializes the supplied object with its object tile and configures
     * any necessary peripheral scene business that couldn't be configured
     * prior to the object having its tile.
     */
    protected void initObject (DisplayObjectInfo info, ObjectTile tile)
    {
        // configure the object info with its object tile
        info.setObjectTile(tile);

        // generate a "shadow" for this object tile by toggling the
        // "covered" flag on in all base tiles below it (to prevent
        // sprites from walking on those tiles)
        setObjectTileFootprint(info.tile, info.x, info.y, true);
    }

    /**
     * Locates the display object info record for the object tile at the
     * specified location. Two of the same kind of object tile cannot
     * exist at the same location.
     */
    protected DisplayObjectInfo getObjectInfo (ObjectTile tile, int x, int y)
    {
        int ocount = _objects.size();
        for (int ii = 0; ii < ocount; ii++) {
            DisplayObjectInfo oinfo = (DisplayObjectInfo)_objects.get(ii);
            if (oinfo.tile == tile && oinfo.x == x && oinfo.y == y) {
                return oinfo;
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
