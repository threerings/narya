//
// $Id: EditableMisoScene.java,v 1.19 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.tools;

import java.awt.Rectangle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.client.DisplayMisoScene;
import com.threerings.miso.client.DisplayObjectInfo;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileSet;

/**
 * The editable Miso scene interface is used in the offline scene building
 * tools as well as by the tools that load those prototype scenes into the
 * runtime database. Accordingly, it provides a means for modifying scene
 * values and for obtaining access to the underlying scene models that
 * represent the underlying scene information.
 *
 * @see DisplayMisoScene
 */
public interface EditableMisoScene
    extends DisplayMisoScene
{
    /**
     * Returns the default base tile.
     */
    public BaseTileSet getDefaultBaseTileSet ();

    /**
     * Sets the default base tile.
     *
     * @param defaultBaseTile the new default base tile.
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the new default base tile.
     */
    public void setDefaultBaseTileSet (BaseTileSet defaultBaseTileSet,
                                       int setId);

    /**
     * Updates the tile at the specified location in the base layer.
     *
     * @param x the x-coordinate of the tile to set.
     * @param y the y-coordinate of the tile to set.
     * @param tile the tile to set.
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the new default base tile.
     */
    public void setBaseTile (int x, int y, BaseTile tile, int fqTileId);

    /**
     * Fill a rectangular area with random tiles from
     * the specified base tileset.
     */
    public void setBaseTiles (Rectangle r, BaseTileSet set, int setId);

    /**
     * Adds an object to this scene.
     *
     * @param x the object's origin x-coordinate.
     * @param y the object's origin y-coordinate.
     * @param tile the tile to set.
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the new default base tile.
     *
     * @return the new scene object instance.
     */
    public DisplayObjectInfo addObject (
        ObjectTile tile, int x, int y, int fqTileId);

    /**
     * Clears out the tile at the specified location in the base layer.
     */
    public void clearBaseTile (int x, int y);

    /**
     * Removes the specified object from the scene.
     */
    public boolean removeObject (DisplayObjectInfo scobj);

    /**
     * Returns a reference to the miso scene model that reflects the
     * changes that have been made to this editable miso scene.
     */
    public MisoSceneModel getMisoSceneModel ();

    /**
     * Replaces the model in use by this editable miso scene with the
     * specified model.
     */
    public void setMisoSceneModel (MisoSceneModel model);
}
