//
// $Id: DisplayMisoSceneImpl.java,v 1.48 2001/12/05 07:29:06 mdb Exp $

package com.threerings.miso.scene;

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
import com.threerings.miso.tile.ShadowTile;

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

            // we have to generate a shadow for this object tile in the
            // base layer so that we can prevent sprites from walking on
            // the object
            setObjectTileFootprint(otile, col, row, new ShadowTile(col, row));
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
     * Place the given tile in the footprint of the object tile at the
     * given coordinates in the scene.
     *
     * @param otile the object tile whose footprint should be set.
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     * @param stamp the tile to place in the object footprint.
     */
    protected void setObjectTileFootprint (
        ObjectTile otile, int x, int y, BaseTile stamp)
    {
        int endx = Math.max(0, (x - otile.getBaseWidth() + 1));
        int endy = Math.max(0, (y - otile.getBaseHeight() + 1));

        for (int xx = x; xx >= endx; xx--) {
            for (int yy = y; yy >= endy; yy--) {
                _base.setTile(xx, yy, stamp);
            }
        }

        // Log.info("Set object tile footprint [tile=" + otile + ", sx=" + x +
        // ", sy=" + y + ", ex=" + endx + ", ey=" + endy + "].");
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
}
