//
// $Id: DisplayMisoSceneImpl.java,v 1.50 2002/02/06 17:13:06 mdb Exp $

package com.threerings.miso.scene;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.ObjectTileLayer;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileLayer;

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
    public DisplayMisoSceneImpl (MisoSceneModel model, TileManager tmgr)
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
        _object = new ObjectTileLayer(new ObjectTile[swid*shei], swid, shei);

        // create a mapping for the action strings and populate it
        _actions = new HashIntMap();
        for (int i = 0; i < model.objectActions.length; i++) {
            String action = model.objectActions[i];
            // skip null or blank actions
            if (StringUtil.blank(action)) {
                continue;
            }
            // the key is the composite of the column and row
            _actions.put(objectKey(model.objectTileIds[3*i],
                                   model.objectTileIds[3*i+1]), action);
        }
    }

    /**
     * Provides this display miso scene with a tile manager from which it
     * can load up all of its tiles.
     *
     * @exception NoSuchTileException thrown if the model references a
     * tile which is not available via the supplied tile manager.
     */
    public void setTileManager (TileManager tmgr)
        throws NoSuchTileException, NoSuchTileSetException
    {
        _tmgr = tmgr;
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

        // populate the base and fringe layers
        for (int column = 0; column < shei; column++) {
            for (int row = 0; row < swid; row++) {
                // first do the base layer
                int tsid = _model.baseTileIds[swid*row+column];
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

                // then the fringe layer
                tsid = _model.fringeTileIds[swid*row+column];
                if (tsid > 0) {
                    int tid = (tsid & 0xFFFF);
                    tsid >>= 16;
                    Tile tile = _tmgr.getTile(tsid, tid);
                    _fringe.setTile(column, row, tile);
                }
            }
        }

        // sanity check the object layer info
        int ocount = _model.objectTileIds.length;
        if (ocount % 3 != 0) {
            throw new IllegalArgumentException(
                "model.objectTileIds.length % 3 != 0");
        }

        // now populate the object layer
        for (int i = 0; i < ocount; i+= 3) {
            int col = _model.objectTileIds[i];
            int row = _model.objectTileIds[i+1];
            int tsid = _model.objectTileIds[i+2];
            int tid = (tsid & 0xFFFF);
            tsid >>= 16;

            // create the object tile and stick it into the appropriate
            // spot in the object layer
            ObjectTile otile = (ObjectTile)_tmgr.getTile(tsid, tid);
            _object.setTile(col, row, otile);
            // generate a "shadow" for this object tile by toggling the
            // "covered" flag on in all base tiles below it (to prevent
            // sprites from walking on those tiles)
            setObjectTileFootprint(otile, col, row, true);
        }
    }

    // documentation inherited
    public BaseTileLayer getBaseLayer ()
    {
        return _base;
    }

    // documentation inherited
    public TileLayer getFringeLayer ()
    {
        return _fringe;
    }

    // documentation inherited
    public ObjectTileLayer getObjectLayer ()
    {
        return _object;
    }

    // documentation inherited from interface
    public String getObjectAction (int column, int row)
    {
        if (_actions != null) {
            return (String)_actions.get(objectKey(column, row));
        } else {
            return null;
        }
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
            for (int yy = y; yy >= endy; yy--) {
                BaseTile tile = _base.getTile(xx, yy);
                if (tile != null) {
                    tile.setCovered(covered);
                }
            }
        }

        // Log.info("Set object tile footprint [tile=" + otile + ", sx=" + x +
        // ", sy=" + y + ", ex=" + endx + ", ey=" + endy + "].");
    }

    /**
     * Computes the action table key for the object at the specified
     * column and row.
     */
    protected static int objectKey (int column, int row)
    {
        return (column << 15) + row;
    }

    /** The tile manager from which we load tiles. */
    protected TileManager _tmgr;

    /** The miso scene model from which we obtain our data. */
    protected MisoSceneModel _model;

    /** The base layer of tiles. */
    protected BaseTileLayer _base;

    /** The fringe layer of tiles. */
    protected TileLayer _fringe;

    /** The object layer of tiles. */
    protected ObjectTileLayer _object;

    /** A map from object tile coordinates to action string. */
    protected HashIntMap _actions;
}
