//
// $Id: DisplayMisoScene.java,v 1.9 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.client;

import java.awt.Rectangle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;

import com.threerings.miso.client.util.ObjectSet;
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
     * Populates the supplied object set with info on all objects whose
     * origin falls in the requested region.
     */
    public void getObjects (Rectangle region, ObjectSet set);

    /**
     * Returns true if the supplied traverser can traverse the specified
     * tile coordinate. The traverser is whatever object is passed along
     * to the path finder when a path is being computed. Scene
     * implementations which support custom traversal based on the type of
     * the traverser will want to reflect the traverser's class and act
     * acordingly.
     */
    public boolean canTraverse (Object traverser, int x, int y);
}
