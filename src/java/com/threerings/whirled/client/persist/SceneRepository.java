//
// $Id: SceneRepository.java,v 1.3 2001/11/12 20:56:55 mdb Exp $

package com.threerings.whirled.client.persist;

import java.io.IOException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The scene repository provides access to a persistent repository of
 * scene information. The scene models in the repository can be updated
 * with scene data fetched from the server as well as with new scene
 * bundles that are periodically distributed to bring all clients into
 * sync with the latest snapshot of the scene database.
 *
 * @see SceneModel
 */
public interface SceneRepository
{
    /**
     * Fetches the mode for the scene with the specified scene id.
     *
     * @exception IOException thrown if an error occurs attempting to load
     * the scene data.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public SceneModel loadSceneModel (int sceneId)
        throws IOException, NoSuchSceneException;

    /**
     * Updates this scene model in the repository. This is generally only
     * called when the server has provided us with a newer version of a
     * scene than we previously had in our local repository.
     *
     * @exception IOException thrown if an error occurs attempting to
     * update the scene data.
     */
    public void updateSceneModel (SceneModel model)
        throws IOException;
}
