//
// $Id: DisplayMisoScene.java,v 1.1 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.tile.ObjectTileLayer;
import com.threerings.media.tile.TileLayer;
import com.threerings.miso.tile.MisoTileLayer;

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
    public MisoTileLayer getBaseLayer ();

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
}
