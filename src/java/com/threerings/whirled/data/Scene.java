//
// $Id: Scene.java,v 1.3 2001/09/21 00:21:40 mdb Exp $

package com.threerings.whirled.data;

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
     * Returns the scene's name. Every scene has a descriptive name.
     */
    public String getName ();

    /**
     * Returns the scene ids of all scenes that neighbor this scene. A
     * neighboring scene is one to which the user can traverse from this
     * scene and vice versa.
     */
    public int[] getNeighborIds ();
}
