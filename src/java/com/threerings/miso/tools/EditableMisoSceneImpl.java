//
// $Id: EditableMisoSceneImpl.java,v 1.25 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.tools;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.Log;

import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileSet;
import com.threerings.miso.tile.MisoTileManager;

import com.threerings.miso.client.DisplayMisoSceneImpl;
import com.threerings.miso.client.DisplayObjectInfo;
import com.threerings.miso.client.util.IsoUtil;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;

/**
 * The default implementation of the {@link EditableMisoScene} interface.
 */
public class EditableMisoSceneImpl
    extends DisplayMisoSceneImpl implements EditableMisoScene
{
    /**
     * Constructs an instance that will be used to display and edit the
     * supplied miso scene data. The tiles identified by the scene model
     * will be loaded via the supplied tile manager.
     *
     * @param model the scene data that we'll be displaying.
     * @param tmgr the tile manager from which to load our tiles.
     */
    public EditableMisoSceneImpl (MisoSceneModel model, MisoTileManager tmgr)
    {
        super(model, tmgr);
    }

    /**
     * Constructs an instance that will be used to display and edit the
     * supplied miso scene data. The tiles identified by the scene model
     * will not be loaded until a tile manager is provided via {@link
     * #setTileManager}.
     *
     * @param model the scene data that we'll be displaying.
     */
    public EditableMisoSceneImpl (MisoSceneModel model)
    {
        super(model);
    }

    // documentation inherited
    public void setMisoSceneModel (MisoSceneModel model)
    {
        super.setMisoSceneModel(model);
    }

    // documentation inherited
    public BaseTileSet getDefaultBaseTileSet ()
    {
        return _defaultBaseTileSet;
    }

    // documentation inherited
    public void setDefaultBaseTileSet (BaseTileSet defaultBaseTileSet,
                                       int setId)
    {
        _defaultBaseTileSet = defaultBaseTileSet;
        _defaultBaseTileSetId = setId;
    }

    // documentation inherited
    public void setBaseTiles (Rectangle r, BaseTileSet set, int setId)
    {
        int setcount = set.getTileCount();

        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                try {
                    int index = _rando.nextInt(setcount);
                    _base.setTile(x, y, (BaseTile) set.getTile(index));
                    _model.setBaseTile(
                        x, y, TileUtil.getFQTileId(setId, index));

                } catch (NoSuchTileException nste) {
                    // not going to happen
                    Log.warning("A tileset is lying to me " +
                                "[error=" + nste + "].");
                }
            }
        }

        _fringer.fringe(_model, _fringe, r, _rando);
    }

    // documentation inherited
    public void setBaseTile (int x, int y, BaseTile tile, int fqTileId)
    {
        _base.setTile(x, y, tile);
        _model.setBaseTile(x, y, fqTileId);
        _fringer.fringe(_model, _fringe, new Rectangle(x, y, 1, 1), _rando);
    }

    // documentation inherited
    public DisplayObjectInfo addObject (
        ObjectTile tile, int x, int y, int fqTileId)
    {
        // sanity check
        if (x > Short.MAX_VALUE || y > Short.MAX_VALUE ||
            x < Short.MIN_VALUE || y < Short.MIN_VALUE) {
            throw new IllegalArgumentException(
                "Invalid tile coordinates [x=" + x + ", y=" + y + "]");
        }

        // create a scene object record and add it to the list
        DisplayObjectInfo info = new DisplayObjectInfo(fqTileId, x, y);
        initObject(info, tile);
        _objects.add(info);

        return info;
    }

    // documentation inherited
    public void clearBaseTile (int x, int y)
    {
        // implemented as a set of one of the random base tiles
        setBaseTiles(new Rectangle(x, y, 1, 1),
                     _defaultBaseTileSet, _defaultBaseTileSetId);
    }

    // documentation inherited
    public boolean removeObject (DisplayObjectInfo info)
    {
        if (_objects.remove(info)) {
            // toggle the "covered" flag off on the base tiles in this object
            // tile's footprint
            setObjectTileFootprint(info.tile, info.x, info.y, false);
            return true;
        } else {
            return false;
        }
    }

    // documentation inherited
    public MisoSceneModel getMisoSceneModel ()
    {
        // we need to flush the object layer to the model prior to
        // returning it; first split our objects into two lists
        ArrayList ilist = new ArrayList();
        ArrayList ulist = new ArrayList();
        for (int ii = 0, ll = _objects.size(); ii < ll; ii++) {
            ObjectInfo info = (ObjectInfo)_objects.get(ii);
            if (info.isInteresting()) {
                // convert to a plain object info record
                ilist.add(new ObjectInfo(info));
            } else {
                ulist.add(info);
            }
        }

        // now populate the scene model appropriately
        MisoSceneModel.populateObjects(_model, ilist, ulist);

        // and we're ready to roll
        return _model;
    }

    /** The default tileset with which to fill the base layer. */
    protected BaseTileSet _defaultBaseTileSet;

    /** The tileset id of the default base tileset. */
    protected int _defaultBaseTileSetId;
}
