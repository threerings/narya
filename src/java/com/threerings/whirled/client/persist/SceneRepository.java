//
// $Id: SceneRepository.java,v 1.4 2003/02/12 07:23:30 mdb Exp $

package com.threerings.whirled.client.persist;

import java.io.IOException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The scene repository provides access to a persistent repository of
 * scene information.
 *
 * @see SceneModel
 */
public interface SceneRepository
{
    /**
     * Fetches the model for the scene with the specified id.
     *
     * @exception IOException thrown if an error occurs attempting to load
     * the scene data.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public SceneModel loadSceneModel (int sceneId)
        throws IOException, NoSuchSceneException;

    /**
     * Updates or inserts this scene model as appropriate.
     *
     * @exception IOException thrown if an error occurs attempting to
     * access the repository.
     */
    public void storeSceneModel (SceneModel model)
        throws IOException;

    /**
     * Deletes the specified scene model from the repository.
     *
     * @exception IOException thrown if an error occurs attempting to
     * access the repository.
     */
    public void deleteSceneModel (int sceneId)
        throws IOException;
}
