//
// $Id: MisoSceneModel.java,v 1.22 2004/08/27 02:20:06 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.data;

import java.awt.Rectangle;
import java.util.Random;

import com.threerings.io.TrackedStreamableObject;

import com.threerings.miso.util.ObjectSet;

/**
 * Contains basic information for a miso scene model that is shared among
 * the specialized model implementations.
 */
public abstract class MisoSceneModel extends TrackedStreamableObject
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
     * Updates the default base tileset id for this scene.
     */
    public void setDefaultBaseTileSet (int tileSetId)
    {
        // nothing doing
    }

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
     *
     * @return true if the object was added, false if the add was rejected
     * due to being a duplicate.
     */
    public abstract boolean addObject (ObjectInfo info);

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
