//
// $Id: SceneRepository.java,v 1.6 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.UpdateList;

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
     * Fetches the set of updates associated with the specified scene.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to load the scene updates.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public UpdateList loadUpdates (int sceneId)
        throws PersistenceException, NoSuchSceneException;

    /**
     * Adds the supplied scene update to the list of updates for its
     * associated scene.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to store the scene update.
     */
    public void addUpdate (SceneUpdate update)
        throws PersistenceException;
}
