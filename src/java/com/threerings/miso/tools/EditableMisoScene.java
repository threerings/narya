//
// $Id: EditableMisoScene.java,v 1.14 2002/04/09 18:06:37 ray Exp $

package com.threerings.miso.scene.tools;

import java.awt.Rectangle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;

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
     * com.threerings.media.tile.TileUtil#getFQTileId}) of the new default
     * base tile.
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
     * com.threerings.media.tile.TileUtil#getFQTileId}) of the new default
     * base tile.
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
     * com.threerings.media.tile.TileUtil#getFQTileId}) of the new default
     * base tile.
     */
    public void setFringeTile (int x, int y, Tile tile, int fqTileId);

    /**
     * Updates the tile at the specified location in the object layer. Any
     * previous object tile at that location should be cleared out by the
     * implementation of this method before the new tile is set to ensure
     * that footprint tiles associated with the old object are properly
     * disposed of.
     *
     * @param x the x-coordinate of the tile to set.
     * @param y the y-coordinate of the tile to set.
     * @param tile the tile to set.
     * @param fqTileId the fully-qualified tile id (@see
     * com.threerings.media.tile.TileUtil#getFQTileId}) of the new default
     * base tile.
     */
    public void setObjectTile (int x, int y, ObjectTile tile, int fqTileId);

    /**
     * Sets the action string for the object tile at the specified
     * coordinates. It may be assumed by the implementation that an object
     * tile exists in the scene at the specified coordinates, thus callers
     * should be sure only to call this method accordingly.
     */
    public void setObjectAction (int x, int y, String action);

    /**
     * Clears out the tile at the specified location in the base layer.
     */
    public void clearBaseTile (int x, int y);

    /**
     * Clears out the tile at the specified location in the fringe layer.
     */
    public void clearFringeTile (int x, int y);

    /**
     * Clears out the tile at the specified location in the object layer.
     */
    public void clearObjectTile (int x, int y);

    /**
     * Clears the action string for the object tile at the specified
     * coordinates. It may be assumed by the implementation that an object
     * tile exists in the scene at the specified coordinates, thus callers
     * should be sure only to call this method accordingly.
     */
    public void clearObjectAction (int x, int y);

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
