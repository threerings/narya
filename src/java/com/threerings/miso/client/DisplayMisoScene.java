//
// $Id: DisplayMisoScene.java,v 1.4 2002/02/17 08:01:15 mdb Exp $

package com.threerings.miso.scene;

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
     * Returns the fring tile at the specified coordinates.
     */
    public Tile getFringeTile (int x, int y);

    /**
     * Returns the object tile at the specified coordinates.
     */
    public ObjectTile getObjectTile (int x, int y);

    /**
     * Returns the action associated with the object tile at the specified
     * column and row. Null is returned if no object tile exists at that
     * column and row or if the object tile that does exist does not have
     * an associated action.
     */
    public String getObjectAction (int column, int row);
}
