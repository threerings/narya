//
// $Id: DisplayMisoScene.java,v 1.6 2002/09/18 02:32:57 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.util.Iterator;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
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
     * Returns the number of object tiles in the scene.
     */
    public int getObjectCount ();

    /**
     * Returns the object tile with the specified index.
     */
    public ObjectTile getObjectTile (int index);

    /**
     * Returns the tile coordinates for the specified object tile.
     *
     * @param index the index of the object tile for which coordinates are
     * desired.
     */
    public Point getObjectCoords (int index);

    /**
     * Returns the action associated with the specified object tile. Null
     * is returned if the object tile does not have an associated action.
     *
     * @param index the index of the object for which the action is
     * desired.
     */
    public String getObjectAction (int index);
}
