//
// $Id: SceneRepository.java,v 1.3 2001/09/21 03:15:02 mdb Exp $

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.util.NoSuchSceneException;

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
