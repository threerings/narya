//
// $Id: Scene.java,v 1.6 2001/10/11 04:07:54 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;

/**
 * The base scene interface. This encapsulates the minimum information
 * needed about a scene in the Whirled system.
 */
public interface Scene
{
    /**
     * Returns the scene's unique identifier.
     */
    public int getId ();

    /**
     * Returns the scene's version. The version should be updated every
     * time the scene is modified and stored back to the repository. This
     * allows a client to determine whether or not they need an updated
     * version of the scene for their local cache.
     */
    public int getVersion ();

    /**
     * Returns the scene ids of all scenes that neighbor this scene. A
     * neighboring scene is one to which the user can traverse from this
     * scene and vice versa.
     */
    public int[] getNeighborIds ();

    /**
     * A scene is associated with a place on the server. Because the
     * scenes are loaded on demand, the scene implementation must be able
     * to provide a place config instance for each scene. This will allow
     * the server to figure out what manager class to instantiate to
     * manage the scene and all the client to figure out what controller
     * class to instantiate to controll the scene. Additionally, it may
     * contain runtime configuration information needed by the application
     * using the Whirled services.
     */
    public PlaceConfig getPlaceConfig ();
}
