//
// $Id: DisplayMisoSceneImpl.java,v 1.58 2002/05/17 19:06:23 ray Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;

import com.threerings.miso.Log;
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
     *
     * @exception NoSuchTileException thrown if the model references a
     * tile which is not available via the supplied tile manager.
     */
    public DisplayMisoSceneImpl (MisoSceneModel model, MisoTileManager tmgr)
        throws NoSuchTileException, NoSuchTileSetException
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
     *
     * @exception NoSuchTileException thrown if the model references a
     * tile which is not available via the supplied tile manager.
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
    }

    /**
     * Provides this display miso scene with a tile manager from which it
     * can load up all of its tiles.
     *
     * @exception NoSuchTileException thrown if the model references a
     * tile which is not available via the supplied tile manager.
     */
    public void setTileManager (MisoTileManager tmgr)
        throws NoSuchTileException, NoSuchTileSetException
    {
        _tmgr = tmgr;
        _fringer = tmgr.getAutoFringer();
        populateLayers();
    }

    /**
     * Populates the tile layers with tiles from the tile manager.
     *
     * @exception NoSuchTileException thrown if the model references a
     * tile which is not available via the supplied tile manager.
     */
    protected void populateLayers ()
        throws NoSuchTileException, NoSuchTileSetException
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
                    BaseTile mtile = (BaseTile)_tmgr.getTile(tsid, tid);
                    _base.setTile(column, row, mtile);
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
        for (int ii = 0; ii < ocount; ii+= 3) {
            int col = _model.objectTileIds[ii];
            int row = _model.objectTileIds[ii+1];
            String action = _model.objectActions[ii/3];
            int fqTid = _model.objectTileIds[ii+2];
            int tsid = (fqTid >> 16) & 0xFFFF;
            int tid = (fqTid & 0xFFFF);
            expandObject(col, row, tsid, tid, fqTid, action);
        }
    }

    /**
     * Called to expand each object read from the model into an actual
     * object tile instance with the appropriate additional data.
     *
     * @return the newly created object tile (which will have been put
     * into all the appropriate lists and tables).
     */
    protected ObjectTile expandObject (
        int col, int row, int tsid, int tid, int fqTid, String action)
        throws NoSuchTileException, NoSuchTileSetException
    {
        // create the object tile and stick it in the list
        ObjectTile otile = (ObjectTile)_tmgr.getTile(tsid, tid);
        _objects.add(otile);
        _coords.put(otile, new Point(col, row));

        // stick the action in the actions table if there is one
        if (!StringUtil.blank(action)) {
            _actions.put(otile, action);
        }

        // generate a "shadow" for this object tile by toggling the
        // "covered" flag on in all base tiles below it (to prevent
        // sprites from walking on those tiles)
        setObjectTileFootprint(otile, col, row, true);

        // return the object tile so that derived classes have easy access
        // to it
        return otile;
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
    public Iterator getObjectTiles ()
    {
        return _objects.iterator();
    }

    // documentation inherited from interface
    public Point getObjectCoords (ObjectTile tile)
    {
        return (Point)_coords.get(tile);
    }

    // documentation inherited from interface
    public String getObjectAction (ObjectTile tile)
    {
        return (String)_actions.get(tile);
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

                BaseTile tile = _base.getTile(xx, yy);
                if (tile != null) {
                    tile.setCovered(covered);
                }
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

    /** The object tiles. */
    protected ArrayList _objects = new ArrayList();

    /** A map from object tile to coordinate records. */
    protected HashMap _coords = new HashMap();

    /** A map from object tile to action string. */
    protected HashMap _actions = new HashMap();

    /** The autofringer. */
    protected AutoFringer _fringer;

    /** A random number generator for filling random base tiles and fringes. */
    protected Random _rando = new Random();
}
