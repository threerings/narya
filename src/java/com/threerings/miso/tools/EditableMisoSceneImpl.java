//
// $Id: EditableMisoSceneImpl.java,v 1.16 2002/04/09 18:06:37 ray Exp $

package com.threerings.miso.scene.tools;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Random;

import com.samskivert.util.HashIntMap;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.Log;

import com.threerings.miso.tile.AutoFringer;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileSet;
import com.threerings.miso.tile.MisoTileManager;

import com.threerings.miso.scene.DisplayMisoSceneImpl;
import com.threerings.miso.scene.MisoSceneModel;
import com.threerings.miso.scene.util.IsoUtil;

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
     *
     * @exception NoSuchTileException thrown if the model references a
     * tile which is not available via the supplied tile manager.
     */
    public EditableMisoSceneImpl (MisoSceneModel model, MisoTileManager tmgr)
        throws NoSuchTileException, NoSuchTileSetException
    {
        super(model, tmgr);
        _fringer = tmgr.getAutoFringer();
        unpackObjectLayer();
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
        unpackObjectLayer();
    }

    // documentation inherited
    public void setMisoSceneModel (MisoSceneModel model)
    {
        super.setMisoSceneModel(model);
        unpackObjectLayer();
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
                    _model.baseTileIds[_model.width*y + x] =
                        TileUtil.getFQTileId(setId, index);

                } catch (NoSuchTileException nste) {
                    // not going to happen
                    Log.warning("A tileset is lying to me [error=" + nste +
                                "].");
                }
            }
        }

        _fringer.fringe(_model, _fringe, r, _rando);
    }

    // documentation inherited
    public void setBaseTile (int x, int y, BaseTile tile, int fqTileId)
    {
        _base.setTile(x, y, tile);
        _model.baseTileIds[_model.width*y + x] = fqTileId;
        _fringer.fringe(_model, _fringe, new Rectangle(x, y, 1, 1), _rando);
    }

    // documentation inherited
    public void setFringeTile (int x, int y, Tile tile, int fqTileId)
    {
        _fringe.setTile(x, y, tile);
        // update the model as well
        _model.fringeTileIds[_model.width*y + x] = fqTileId;
    }

    // documentation inherited
    public void setObjectTile (int x, int y, ObjectTile tile, int fqTileId)
    {
        ObjectTile prev = _object.getTile(x, y);
        // clear out any previous tile to ensure that everything is
        // properly cleaned up
        if (prev != null) {
            clearObjectTile(x, y);
        }
        // stick the new object into the layer
        _object.setTile(x, y, tile);
        // toggle the "covered" flag on in all base tiles below this
        // object tile
        setObjectTileFootprint(tile, x, y, true);
        // stick this value into our non-sparse object layer
        _objectTileIds.put(IsoUtil.coordsToKey(x, y), new Integer(fqTileId));
    }

    // documentation inherited from interface
    public void setObjectAction (int x, int y, String action)
    {
        _actions.put(objectKey(x, y), action);
    }

    // documentation inherited
    public void clearBaseTile (int x, int y)
    {
        // implemented as a set of one of the random base tiles
        setBaseTiles(new Rectangle(x, y, 1, 1),
                     _defaultBaseTileSet, _defaultBaseTileSetId);
    }

    // documentation inherited
    public void clearFringeTile (int x, int y)
    {
        _fringe.setTile(x, y, null);
        // clear it out in the model
        _model.fringeTileIds[_model.width*y + x] = 0;
    }

    // documentation inherited
    public void clearObjectTile (int x, int y)
    {
        ObjectTile tile = _object.getTile(x, y);
        if (tile != null) {
            // toggle the "covered" flag off on the base tiles in this
            // object tile's footprint
            setObjectTileFootprint(tile, x, y, false);
            // clear out the tile itself
            _object.clearTile(x, y);
        }
        // clear it out in our non-sparse array
        _objectTileIds.remove(IsoUtil.coordsToKey(x, y));
        // clear out any action for this tile as well
        _actions.remove(objectKey(x, y));
    }

    // documentation inherited from interface
    public void clearObjectAction (int x, int y)
    {
        _actions.remove(objectKey(x, y));
    }

    // documentation inherited
    public MisoSceneModel getMisoSceneModel ()
    {
        // we need to flush the object layer to the model prior to
        // returning it
        int otileCount = _objectTileIds.size();
        int cols = _object.getWidth();
        int rows = _object.getHeight();

        // now create and populate the new tileid and actions arrays
        int[] otids = new int[otileCount*3];
        String[] actions = new String[otileCount];
        int otidx = 0, actidx = 0;

        Iterator keys = _objectTileIds.keys();
        while (keys.hasNext()) {
            int key = ((Integer) keys.next()).intValue();
            int c = IsoUtil.xCoordFromKey(key);
             int r = IsoUtil.yCoordFromKey(key);
            otids[otidx++] = c;
            otids[otidx++] = r;
            otids[otidx++] = ((Integer) _objectTileIds.get(key)).intValue();
            actions[actidx++] = (String)_actions.get(objectKey(c, r));
        }

        // stuff the new arrays into the model
        _model.objectTileIds = otids;
        _model.objectActions = actions;

        // and we're ready to roll
        return _model;
    }

    /**
     * Unpacks the object layer into an array that we can update along
     * with the other layers.
     */
    protected void unpackObjectLayer ()
    {
        // we need this to track object layer mods
        _objectTileIds = new HashIntMap();

        // populate our non-spare array
        int[] otids = _model.objectTileIds;
        for (int i = 0; i < otids.length; i += 3) {
            int x = otids[i];
            int y = otids[i+1];
            int fqTileId = otids[i+2];
            _objectTileIds.put(IsoUtil.coordsToKey(x, y),
                               new Integer(fqTileId));
        }
    }

    /** where we keep track of object tile ids. */
    protected HashIntMap _objectTileIds;

    /** The default tileset with which to fill the base layer. */
    protected BaseTileSet _defaultBaseTileSet;

    /** The tileset id of the default base tileset. */
    protected int _defaultBaseTileSetId;

    /** The autofringer. */
    protected AutoFringer _fringer;

    /** A random number generator for filling random base tiles and fringes. */
    protected Random _rando = new Random();
}
