//
// $Id: SceneService.java,v 1.5 2001/10/11 04:07:54 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.whirled.Log;

/**
 * The scene service class provides the client interface to the scene
 * related invocation services (e.g. moving from scene to scene).
 */
public class SceneService implements SceneCodes
{
    /**
     * Requests that that this client's body be moved to the specified
     * scene.
     *
     * @param sceneId the scene id to which we want to move.
     * @param sceneVers the version number of the scene object that we
     * have in our local repository.
     */
    public static void moveTo (Client client, int sceneId,
                               int sceneVers, SceneDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] {
            new Integer(sceneId), new Integer(sceneVers) };
        invdir.invoke(MODULE_NAME, MOVE_TO_REQUEST, args, rsptarget);
        Log.info("Sent moveTo request [scene=" + sceneId +
                 ", version=" + sceneVers + "].");
    }
}
