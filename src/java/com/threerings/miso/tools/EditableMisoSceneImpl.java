//
// $Id: EditableMisoSceneImpl.java,v 1.21 2002/09/12 21:10:31 mdb Exp $

package com.threerings.miso.scene.tools;

import java.awt.Point;
import java.awt.Rectangle;

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
    public void addObjectTile (ObjectTile tile, int x, int y, int fqTileId)
    {
        // add the tile to the list
        _objects.add(tile);
        _coords.put(tile, new Point(x, y));
        _objectTileIds.put(tile, new Integer(fqTileId));

        // toggle the "covered" flag on in all base tiles below this
        // object tile
        setObjectTileFootprint(tile, x, y, true);
    }

    // documentation inherited from interface
    public void setObjectAction (ObjectTile tile, String action)
    {
        _actions.put(tile, action);
    }

    // documentation inherited
    public void clearBaseTile (int x, int y)
    {
        // implemented as a set of one of the random base tiles
        setBaseTiles(new Rectangle(x, y, 1, 1),
                     _defaultBaseTileSet, _defaultBaseTileSetId);
    }

    // documentation inherited
    public void removeObjectTile (ObjectTile tile)
    {
        // remove the tile from the list and tables
        _objects.remove(tile);
        _actions.remove(tile);
        _objectTileIds.remove(tile);

        Point p = (Point)_coords.remove(tile);
        if (p != null) {
            // toggle the "covered" flag off on the base tiles in this
            // object tile's footprint
            setObjectTileFootprint(tile, p.x, p.y, false);
        }
    }

    // documentation inherited from interface
    public void clearObjectAction (ObjectTile tile)
    {
        _actions.remove(tile);
    }

    // documentation inherited
    public MisoSceneModel getMisoSceneModel ()
    {
        // we need to flush the object layer to the model prior to
        // returning it
        int ocount = _objects.size();

        // but only do it if we've actually got some objects
        if (ocount > 0) {
            int[] otids = new int[ocount*3];
            String[] actions = new String[ocount];

            for (int ii = 0; ii < ocount; ii++) {
                ObjectTile tile = (ObjectTile)_objects.get(ii);
                Point coords = (Point)_coords.get(tile);
                otids[3*ii] = coords.x;
                otids[3*ii+1] = coords.y;
                otids[3*ii+2] = ((Integer)_objectTileIds.get(tile)).intValue();
                actions[ii] = (String)_actions.get(tile);
            }

            // stuff the new arrays into the model
            _model.objectTileIds = otids;
            _model.objectActions = actions;
        }

        // and we're ready to roll
        return _model;
    }

    // documentation inherited
    protected ObjectTile expandObject (
        int col, int row, int tsid, int tid, int fqTid, String action)
        throws NoSuchTileException, NoSuchTileSetException
    {
        // do the actual object creation
        ObjectTile tile = super.expandObject(
            col, row, tsid, tid, fqTid, action);

        // make sure our array is created (we have to do this specially
        // here because this method is called before our constructor is
        // called; yay Java!)
        if (_objectTileIds == null) {
            _objectTileIds = new HashMap();
        }

        // we need this to track object layer mods
        _objectTileIds.put(tile, new Integer(fqTid));

        // pass on the objecty goodness
        return tile;
    }

    /** Where we keep track of object tile ids. */
    protected HashMap _objectTileIds = new HashMap();

    /** The default tileset with which to fill the base layer. */
    protected BaseTileSet _defaultBaseTileSet;

    /** The tileset id of the default base tileset. */
    protected int _defaultBaseTileSetId;
}
