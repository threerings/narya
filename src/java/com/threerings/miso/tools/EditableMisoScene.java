//
// $Id: EditableMisoScene.java,v 1.15 2002/04/27 18:41:14 mdb Exp $

package com.threerings.miso.scene.tools;

import java.awt.Rectangle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileSet;
import com.threerings.miso.scene.DisplayMisoScene;
import com.threerings.miso.scene.MisoSceneModel;

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
     * Updates the tile at the specified location in the fringe layer.
     *
     * @param x the x-coordinate of the tile to set.
     * @param y the y-coordinate of the tile to set.
     * @param tile the tile to set.
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the new default base tile.
     */
    public void setFringeTile (int x, int y, Tile tile, int fqTileId);

    /**
     * Addds an object tile to this scene.
     *
     * @param x the object's origin x-coordinate.
     * @param y the object's origin y-coordinate.
     * @param tile the tile to set.
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the new default base tile.
     */
    public void addObjectTile (ObjectTile tile, int x, int y, int fqTileId);

    /**
     * Sets the action string for the specified object tile.
     */
    public void setObjectAction (ObjectTile tile, String action);

    /**
     * Clears out the tile at the specified location in the base layer.
     */
    public void clearBaseTile (int x, int y);

    /**
     * Clears out the tile at the specified location in the fringe layer.
     */
    public void clearFringeTile (int x, int y);

    /**
     * Clears out the specified tile from the object list.
     */
    public void removeObjectTile (ObjectTile tile);

    /**
     * Clears the action string for the specified object tile.
     */
    public void clearObjectAction (ObjectTile tile);

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
