//
// $Id: SceneRepository.java,v 1.4 2001/09/28 22:15:57 mdb Exp $

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.data.Scene;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The scene repository provides the basic interface for loading and
 * updating scenes. It is used by the scene registry and though more scene
 * related persistence services may be needed in a full-fledged
 * application, the scene repository only encapsulates those needed by the
 * scene registry and other services provided by the Whirled framework.
 */
public interface SceneRepository
{
    /**
     * Fetches the scene with the specified scene id.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to load a scene.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public Scene loadScene (int sceneId)
        throws PersistenceException, NoSuchSceneException;

    /**
     * Updates the specified scene in the repository with the information
     * provided in the scene object.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to update the scene.
     */
    public void updateScene (Scene scene)
        throws PersistenceException;
}
