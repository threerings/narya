//
// $Id: SceneRepository.java,v 1.5 2001/11/12 20:56:56 mdb Exp $

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The scene repository provides the basic interface for loading and
 * updating scene data. It is used by the scene registry and though more
 * scene related persistence services may be needed in a full-fledged
 * application, the scene repository only encapsulates those needed by the
 * scene registry and other services provided by the Whirled framework.
 */
public interface SceneRepository
{
    /**
     * Fetches the model for the scene with the specified scene id.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to load the scene data.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public SceneModel loadSceneModel (int sceneId)
        throws PersistenceException, NoSuchSceneException;

    /**
     * Updates the specified scene model in the repository.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to update the scene data.
     */
    public void updateSceneModel (SceneModel scene)
        throws PersistenceException;
}
