//
// $Id: SceneService.java,v 1.2 2001/08/14 06:51:07 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationManager;

import com.threerings.whirled.Log;

/**
 * The scene service class provides the client interface to the scene
 * related invocation services (e.g. moving from scene to scene).
 */
public class SceneService
{
    /** The module name for the scene services. */
    public static final String MODULE = "whirled!scene";

    /**
     * Requests that that this client's body be moved to the specified
     * scene.
     *
     * @param sceneId the scene id to which we want to move.
     * @param sceneVers the version number of the scene object that we
     * have in our local repository.
     */
    public static void moveTo (Client client, int sceneId,
                               int sceneVers, SceneManager rsptarget)
    {
        InvocationManager invmgr = client.getInvocationManager();
        Object[] args = new Object[] {
            new Integer(sceneId), new Integer(sceneVers) };
        invmgr.invoke(MODULE, "MoveTo", args, rsptarget);
        Log.info("Sent moveTo request [scene=" + sceneId +
                 ", version=" + sceneVers + "].");
    }
}
