//
// $Id: DisplayMisoScene.java,v 1.7 2002/09/23 21:54:50 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Rectangle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;

import com.threerings.miso.scene.util.ObjectSet;
import com.threerings.miso.tile.BaseTile;

/**
 * Makes available the information from the {@link MisoSceneModel} in a
 * form that is amenable to actually displaying the scene in a user
 * interface. As with all display scene implementations, the information
 * provided is read-only and should never be modified by the caller.
 */
public interface DisplayMisoScene
{
    /**
     * Returns the base tile at the specified coordinates.
     */
    public BaseTile getBaseTile (int x, int y);

    /**
     * Returns the fringe tile at the specified coordinates.
     */
    public Tile getFringeTile (int x, int y);

    /**
     * Populates the supplied scene object set with all objects whose
     * origin falls in the requested region.
     */
    public void getSceneObjects (Rectangle region, ObjectSet set);
}
