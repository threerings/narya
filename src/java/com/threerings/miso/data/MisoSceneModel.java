//
// $Id: MisoSceneModel.java,v 1.17 2003/04/21 17:08:57 mdb Exp $

package com.threerings.miso.data;

import java.awt.Rectangle;
import java.util.Random;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.media.tile.TileUtil;

import com.threerings.miso.util.ObjectSet;

/**
 * Contains basic information for a miso scene model that is shared among
 * the specialized model implementations.
 */
public abstract class MisoSceneModel extends SimpleStreamableObject
    implements Cloneable
{
    /**
     * Creates a completely uninitialized model suitable for little more
     * than unserialization.
     */
    public MisoSceneModel ()
    {
    }

    /**
     * Returns the fully qualified tile id of the base tile at the
     * specified coordinates. <code>-1</code> will be returned if there is
     * no tile at the specified coordinate.
     */
    public abstract int getBaseTileId (int x, int y);

    /**
     * Updates the tile at the specified location in the base layer.
     *
     * <p> Note that if there are fringe tiles associated with this scene,
     * calling this method may result in the surrounding fringe tiles
     * being cleared and subsequently recalculated. This should not be
     * called on a displaying scene unless you know what you are doing.
     *
     * @param fqTileId the fully-qualified tile id (@see
     * TileUtil#getFQTileId}) of the tile to set.
     * @param x the x-coordinate of the tile to set.
     * @param y the y-coordinate of the tile to set.
     *
     * @return false if the specified tile coordinates are outside of the
     * scene and the tile was not saved, true otherwise.
     */
    public abstract boolean setBaseTile (int fqTileId, int x, int y);

    /**
     * Scene models can return a default tileset to be used when no base
     * tile data exists for a particular tile.
     */
    public int getDefaultBaseTileSet ()
    {
        return 0;
    }

    /**
     * Populates the supplied object set with info on all objects whose
     * origin falls in the requested region.
     */
    public abstract void getObjects (Rectangle region, ObjectSet set);

    /**
     * Adds an object to this scene.
     */
    public abstract void addObject (ObjectInfo info);

    /**
     * Updates an object in this scene.
     */
    public abstract void updateObject (ObjectInfo info);

    /**
     * Removes the specified object from the scene.
     *
     * @return true if it was removed, false if the object was not in the
     * scene.
     */
    public abstract boolean removeObject (ObjectInfo info);

    /**
     * Creates a copy of this scene model.
     */
    public Object clone ()
    {
        try {
            return (MisoSceneModel)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("MisoSceneModel.clone: " + cnse);
        }
    }

    /** A random number generator for filling random base tiles. */
    protected transient Random _rando = new Random();
}
