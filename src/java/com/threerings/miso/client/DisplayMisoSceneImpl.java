//
// $Id: DisplayMisoSceneImpl.java,v 1.44 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.ObjectTileLayer;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.tile.MisoTile;
import com.threerings.miso.tile.MisoTileLayer;
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
        int swid = model.width;
        int shei = model.height;

        // create the individual tile layer objects
        _base = new MisoTileLayer(new MisoTile[swid*shei], swid, shei);
        _fringe = new TileLayer(new Tile[swid*shei], swid, shei);
        _object = new ObjectTileLayer(new ObjectTile[swid*shei], swid, shei);

        // populate the base and fringe layers
        for (int column = 0; column < shei; column++) {
            for (int row = 0; row < swid; row++) {
                // first do the base layer
                int tsid = model.baseTileIds[swid*row+column];
                if (tsid > 0) {
                    int tid = (tsid & 0xFFFF);
                    tsid >>= 16;
                    // this is a bit magical, but the tile manager will
                    // fetch tiles from the tileset repository and the
                    // tile set id from which we request this tile must
                    // map to a miso tile as provided by the repository,
                    // so we just cast it to a miso tile and know that all
                    // is well
                    MisoTile mtile = (MisoTile)tmgr.getTile(tsid, tid);
                    _base.setTile(column, row, mtile);
                }

                // then the fringe layer
                tsid = model.fringeTileIds[swid*row+column];
                if (tsid > 0) {
                    int tid = (tsid & 0xFFFF);
                    tsid >>= 16;
                    Tile tile = tmgr.getTile(tsid, tid);
                    _fringe.setTile(column, row, tile);
                }
            }
        }

        // sanity check the object layer info
        int ocount = model.objectTileIds.length;
        if (ocount % 3 != 0) {
            throw new IllegalArgumentException(
                "model.objectTileIds.length % 3 != 0");
        }

        // now populate the object layer
        for (int i = 0; i < ocount; i+= 3) {
            int row = model.objectTileIds[i];
            int col = model.objectTileIds[i+1];
            int tsid = model.objectTileIds[i+2];
            int tid = (tsid & 0xFFFF);
            tsid >>= 16;

            // create the object tile and stick it into the appropriate
            // spot in the object layer
            ObjectTile otile = (ObjectTile)tmgr.getTile(tsid, tid);
            _object.setTile(col, row, otile);

            // we have to generate a shadow for this object tile in the
            // base layer so that we can prevent sprites from walking on
            // the object
            generateObjectShadow(row, col);
        }
    }

    // documentation inherited
    public MisoTileLayer getBaseLayer ()
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
     * Place shadow tiles in the footprint of the object tile at the given
     * coordinates in the scene. This method should be called when an
     * object tile is added to the scene.
     *
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     */
    protected void generateObjectShadow (int x, int y)
    {
        setObjectTileFootprint(x, y, new ShadowTile(x, y));
    }

    /**
     * Place the given tile in the footprint of the object tile at the
     * given coordinates in the scene.
     *
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     * @param stamp the tile to place in the object footprint.
     */
    protected void setObjectTileFootprint (int x, int y, MisoTile stamp)
    {
        ObjectTile tile = _object.getTile(y, x);
        int endx = Math.max(0, (x - tile.getBaseWidth() + 1));
        int endy = Math.max(0, (y - tile.getBaseHeight() + 1));

        for (int xx = x; xx >= endx; xx--) {
            for (int yy = y; yy >= endy; yy--) {
                _base.setTile(yy, xx, stamp);
            }
        }
    }

    /** The base layer of tiles. */
    protected MisoTileLayer _base;

    /** The fringe layer of tiles. */
    protected TileLayer _fringe;

    /** The object layer of tiles. */
    protected ObjectTileLayer _object;
}
