//
// $Id: Scene.java,v 1.8 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;

/**
 * This interface makes available basic scene information. At this basic
 * level, not much information is available, but extensions to this
 * interface begin to create a more comprehensive picture of a scene in a
 * system built from the Whirled services.
 */
public interface Scene
{
    /**
     * Returns the unique identifier for this scene.
     */
    public int getId ();

    /**
     * Returns the human readable name of this scene.
     */
    public String getName ();

    /**
     * Returns the version number of this scene.
     */
    public int getVersion ();

    /**
     * Returns the place config that can be used to determine which place
     * controller instance should be used to display this scene as well as
     * to obtain runtime configuration information.
     */
    public PlaceConfig getPlaceConfig ();

    /**
     * Sets this scene's unique identifier.
     */
    public void setId (int sceneId);

    /**
     * Sets the human readable name of this scene.
     */
    public void setName (String name);

    /**
     * Sets this scene's version number.
     */
    public void setVersion (int version);

    /**
     * Returns the scene model from which this scene was created.
     */
    public SceneModel getSceneModel ();
}
