//
// $Id: SceneRepository.java,v 1.2 2001/08/14 06:51:07 mdb Exp $

package com.threerings.whirled.client.persist;

import java.io.IOException;

import com.threerings.whirled.data.Scene;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The scene repository provides access to a persistent repository of
 * scene information. The scenes in the repository can be updated with
 * individual scenes fetched from the server as well as with new scene
 * bundles that are periodically distributed to bring all clients into
 * sync with the latest snapshot of the scene database.
 *
 * @see com.threerings.whirled.data.Scene
 */
public interface SceneRepository
{
    /**
     * Fetches the scene with the specified scene id.
     *
     * @exception IOException thrown if an error occurs attempting to load
     * a scene.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public Scene loadScene (int sceneId)
        throws IOException, NoSuchSceneException;

    /**
     * Updates the specified scene in the repository with the information
     * provided in the scene object.
     *
     * @exception IOException thrown if an error occurs attempting to
     * update the scene.
     */
    public void updateScene (Scene scene)
        throws IOException;
}
