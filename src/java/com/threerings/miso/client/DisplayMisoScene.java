//
// $Id: DisplayMisoScene.java,v 1.3 2002/01/30 18:28:32 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.tile.ObjectTileLayer;
import com.threerings.media.tile.TileLayer;
import com.threerings.miso.tile.BaseTileLayer;

/**
 * Makes available the information from the {@link MisoSceneModel} in a
 * form that is amenable to actually displaying the scene in a user
 * interface. As with all display scene implementations, the information
 * provided is read-only and should never be modified by the caller.
 */
public interface DisplayMisoScene
{
    /**
     * Returns the tiles that comprise the base layer of this scene. This
     * layer is read-only and not to be modified.
     */
    public BaseTileLayer getBaseLayer ();

    /**
     * Returns the tiles that comprise the fringe layer of this scene.
     * This layer is read-only and not to be modified.
     */
    public TileLayer getFringeLayer ();

    /**
     * Returns the tiles that comprise the object layer of this scene.
     * This layer is read-only and not to be modified.
     */
    public ObjectTileLayer getObjectLayer ();

    /**
     * Returns the action associated with the object tile at the specified
     * column and row. Null is returned if no object tile exists at that
     * column and row or if the object tile that does exist does not have
     * an associated action.
     */
    public String getObjectAction (int column, int row);
}
