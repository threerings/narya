//
// $Id: EditableMisoSceneImpl.java,v 1.1 2001/11/18 04:09:23 mdb Exp $

package com.threerings.miso.tools;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.tile.MisoTile;

import com.threerings.miso.scene.DisplayMisoSceneImpl;
import com.threerings.miso.scene.MisoSceneModel;

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
    public EditableMisoSceneImpl (MisoSceneModel model, TileManager tmgr)
        throws NoSuchTileException, NoSuchTileSetException
    {
        super(model, tmgr);

        // we'll need to be keeping this
        _model = model;

        // we need this to track object layer mods
        _objectTileIds = new int[_model.baseTileIds.length];
    }

    // documentation inherited
    public MisoTile getDefaultBaseTile ()
    {
        return _defaultBaseTile;
    }

    // documentation inherited
    public void setDefaultBaseTile (MisoTile defaultBaseTile, int fqTileId)
    {
        _defaultBaseTile = defaultBaseTile;
        _defaultBaseTileId = fqTileId;
    }

    // documentation inherited
    public void setBaseTile (int x, int y, MisoTile tile, int fqTileId)
    {
        _base.setTile(x, y, tile);
        // update the model as well
        _model.baseTileIds[_model.width*y + x] = fqTileId;
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
        // clear out any previous tile so that shadow tiles are properly
        // removed
        if (prev != null) {
            clearObjectTile(x, y);
        }
        _object.setTile(x, y, tile);
        // stick this value into our non-sparse object layer
        _objectTileIds[_model.width*y + x] = fqTileId;
    }

    // documentation inherited
    public void clearBaseTile (int x, int y)
    {
        _base.setTile(x, y, _defaultBaseTile);
        // clear it out in the model
        _model.baseTileIds[_model.width*y + x] = _defaultBaseTileId;
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
        setObjectTileFootprint(x, y, _defaultBaseTile);
        // clear it out in our non-sparse array
        _objectTileIds[_model.width*y + x] = 0;
        // we don't have to worry about setting the footprint in the model
        // because footprints are always inferred from the contents of the
        // object layer and the base layer in the model can simply contain
        // the default tiles
    }

    // documentation inherited
    public MisoSceneModel getModel ()
    {
        // we need to flush the object layer to the model prior to
        // returning it

        return _model;
    }

    /** Our scene model, which we always keep in sync with our display
     * model data. */
    protected MisoSceneModel _model;

    /** A non-sparse array where we can keep track of the object tile
     * ids. */
    protected int[] _objectTileIds;

    /** The default tile with which to fill the base layer. */
    protected MisoTile _defaultBaseTile;

    /** The fully qualified tile id of the default base tile. */
    protected int _defaultBaseTileId;
}
