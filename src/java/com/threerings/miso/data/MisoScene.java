//
// $Id: MisoScene.java,v 1.1 2003/02/12 05:39:15 mdb Exp $

package com.threerings.miso.data;

import java.awt.Rectangle;

import com.threerings.miso.client.util.ObjectSet;

/**
 * Provides information on the composition of tiles in a Miso scene.
 */
public interface MisoScene
{
    /**
     * Returns the fully qualified tile id of the base tile at the
     * specified coordinates. <code>-1</code> will be returned if there is
     * no tile at the specified coordinate.
     */
    public int getBaseTileId (int x, int y);

    /**
     * Populates the supplied object set with info on all objects whose
     * origin falls in the requested region.
     */
    public void getObjects (Rectangle region, ObjectSet set);

    /**
     * Updates the tile at the specified location in the base layer.
     *
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the tile to set.
     * @param x the x-coordinate of the tile to set.
     * @param y the y-coordinate of the tile to set.
     */
    public void setBaseTile (int fqTileId, int x, int y);

    /**
     * Fill a rectangular area with random tiles from the specified base
     * tileset.
     *
     * @param r the region to be filled.
     * @param setId the id of the tileset to use when filling.
     * @param setSize the number of tiles in the tileset.
     */
    public void setBaseTiles (Rectangle r, int setId, int setSize);

    /**
     * Adds an object to this scene.
     *
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the object tile.
     * @param x the object's origin x-coordinate.
     * @param y the object's origin y-coordinate.
     *
     * @return the new object info record.
     */
    public ObjectInfo addObject (int fqTileId, int x, int y);

    /**
     * Removes the specified object from the scene.
     */
    public boolean removeObject (ObjectInfo info);

    /**
     * Returns the scene model used by this scene.
     */
    public MisoSceneModel getSceneModel ();
}
