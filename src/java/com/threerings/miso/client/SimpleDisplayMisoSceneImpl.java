//
// $Id: SimpleDisplayMisoSceneImpl.java,v 1.2 2003/02/20 00:40:13 ray Exp $

package com.threerings.miso.client;

import java.util.HashMap;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;

import com.threerings.miso.Log;
import com.threerings.miso.client.util.ObjectSet;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.data.SimpleMisoSceneImpl;
import com.threerings.miso.data.SimpleMisoSceneModel;
import com.threerings.miso.tile.AutoFringer;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.MisoTileManager;

/**
 * An implementation of the {@link DisplayMisoScene} interface that uses a
 * simple miso scene model.
 */
public class SimpleDisplayMisoSceneImpl extends SimpleMisoSceneImpl
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
    public SimpleDisplayMisoSceneImpl (SimpleMisoSceneModel model,
                                       MisoTileManager tmgr)
    {
        super(model);

        _tmgr = tmgr;
        _fringer = tmgr.getAutoFringer();

        int swid = _model.width;
        int shei = _model.height;

        // create the individual tile layer objects
        _base = new BaseTile[swid*shei];
        _fringe = new Tile[swid*shei];
        _covered = new boolean[swid*shei];

        // load up the tiles for our base layer
        for (int column = 0; column < shei; column++) {
            for (int row = 0; row < swid; row++) {
                int fqTileId = getBaseTileId(column, row);
                if (fqTileId > 0) {
                    populateBaseTile(fqTileId, column, row);
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

    // documentation inherited from interface
    public void setBaseTile (int fqTileId, int x, int y)
    {
        super.setBaseTile(fqTileId, x, y);
        populateBaseTile(fqTileId, x, y);

        // setting a base tile has the side-effect of clearing out the
        // surrounding fringe tiles.
        for (int xx=Math.max(x - 1, 0),
                 xn=Math.min(x + 1, _model.width - 1); xx <= xn; xx++) {
            for (int yy=Math.max(y - 1, 0),
                     yn=Math.min(y + 1, _model.height - 1); yy <= yn; yy++) {
                _fringe[yy * _model.width + xx] = null;
            }
        }
    }

    // documentation inherited from interface
    public ObjectInfo addObject (int fqTileId, int x, int y)
    {
        DisplayObjectInfo info = (DisplayObjectInfo)
            super.addObject(fqTileId, x, y);
        populateObject(info);
        return info;
    }

    // documentation inherited from interface
    public boolean removeObject (ObjectInfo info)
    {
        if (super.removeObject(info)) {
            // clear out this object's "shadow"
            DisplayObjectInfo dinfo = (DisplayObjectInfo)info;
            setObjectTileFootprint(dinfo.tile, dinfo.x, dinfo.y, false);
            return true;
        } else {
            return false;
        }
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        if (x < 0 || y < 0 || x >= _model.width || y >= _model.height) {
            return null;
        }
        return _base[y*_model.width+x];
    }

    // documentation inherited from interface
    public Tile getFringeTile (int x, int y)
    {
        if (x < 0 || y < 0 || x >= _model.width || y >= _model.height) {
            return null;
        }

        // if we have not yet composed this fringe tile, do so
        int idx = y * _model.width + x;
        if (_fringe[idx] == null) {
            _fringe[idx] = _fringer.getFringeTile(this, x, y, _masks, _rando);
            // make a note of non-fringed tiles that have been resolved
            // but have no fringe tile
            if (_fringe[idx] == null) {
                _fringe[idx] = _nullFringe;
            }
        }

        return (_fringe[idx] == _nullFringe) ? null : _fringe[idx];
    }

    // documentation inherited from interface
    public boolean canTraverse (Object trav, int x, int y)
    {
        BaseTile tile = getBaseTile(x, y);
        return (((tile == null) || tile.isPassable()) &&
                !_covered[y*_model.width+x]);
    }

    /**
     * Called to populate a coordinate with its base tile.
     */
    protected void populateBaseTile (int fqTileId, int x, int y)
    {
        if (x < 0 || y < 0 || x >= _model.width || y >= _model.height) {
            // nothing doing
            return;
        }
        int tsid = fqTileId >> 16, tid = (fqTileId & 0xFFFF);

        // this is a bit magical, but the tile manager will fetch tiles
        // from the tileset repository and the tile set id from which we
        // request this tile must map to a base tile as provided by the
        // repository, so we just cast it to a base tile and know that all
        // is well
        String errmsg = null;
        try {
            _base[y*_model.width+x] = (BaseTile)_tmgr.getTile(tsid, tid);
        } catch (ClassCastException cce) {
            errmsg = "Scene contains non-base tile in base layer";
        } catch (NoSuchTileSetException nste) {
            errmsg = "Scene contains non-existent tileset";
        } catch (NoSuchTileException nste) {
            errmsg = "Scene contains non-existent tile";
        }

        if (errmsg != null) {
            Log.warning(errmsg + " [tsid=" + tsid + ", tid=" + tid +
                        ", x=" + x + ", y=" + y + "].");
        }
    }

    /**
     * Called to populate an object with its object tile.
     */
    protected boolean populateObject (DisplayObjectInfo info)
    {
        int tsid = (info.tileId >> 16) & 0xFFFF, tid = (info.tileId & 0xFFFF);
        try {
            initObject(info, (ObjectTile)_tmgr.getTile(tsid, tid));
            return true;
        } catch (NoSuchTileException nste) {
            Log.warning("Scene contains non-existent object tile " +
                        "[info=" + info + "].");
        } catch (NoSuchTileSetException te) {
            Log.warning("Scene contains non-existent object tileset " +
                        "[info=" + info + "].");
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

    // documentation inherited
    protected ObjectInfo createObjectInfo (int tileId, int x, int y)
    {
        return new DisplayObjectInfo(tileId, x, y);
    }

    // documentation inherited
    protected ObjectInfo createObjectInfo (ObjectInfo source)
    {
        return new DisplayObjectInfo(source);
    }

    /** The tile manager from which we load tiles. */
    protected MisoTileManager _tmgr;

    /** The base layer of tiles. */
    protected BaseTile[] _base;

    /** The fringe layer of tiles. */
    protected Tile[] _fringe;

    /** Contains cached fringe mask tiles. TODO: LRU this or something. */
    protected HashMap _masks = new HashMap();

    /** Used to identify non-existent fringe tiles. */
    protected Tile _nullFringe;

    /** Information on which tiles are covered by object tiles. */
    protected boolean[] _covered;

    /** The autofringer. */
    protected AutoFringer _fringer;
}
