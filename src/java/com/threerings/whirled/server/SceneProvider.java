//
// $Id: SceneProvider.java,v 1.11 2002/08/14 19:07:57 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneModel;

/**
 * The scene provider handles the server side of the scene related
 * invocation services (e.g. moving from scene to scene).
 */
public class SceneProvider
    implements InvocationProvider, SceneCodes
{
    /**
     * Constructs a scene provider that will interact with the supplied
     * scene registry.
     */
    public SceneProvider (LocationProvider locprov, SceneRegistry screg)
    {
        _locprov = locprov;
        _screg = screg;
    }

    /**
     * Processes a request from a client to move to a new scene.
     */
    public void moveTo (ClientObject caller, int sceneId,
                        final int sceneVer, final SceneMoveListener listener)
    {
        final BodyObject source = (BodyObject)caller;

        // create a callback object that will handle the resolution or
        // failed resolution of the scene
        SceneRegistry.ResolutionListener rl =
            new SceneRegistry.ResolutionListener()
        {
            public void sceneWasResolved (SceneManager scmgr)
            {
                finishMoveToRequest(source, scmgr, sceneVer, listener);
            }

            public void sceneFailedToResolve (
                int rsceneId, Exception reason)
            {
                Log.warning("Unable to resolve scene [sceneid=" + rsceneId +
                            ", reason=" + reason + "].");
                // pretend like the scene doesn't exist to the client
                listener.requestFailed(NO_SUCH_PLACE);
            }
        };

        // make sure the scene they are headed to is actually loaded into
        // the server
        _screg.resolveScene(sceneId, rl);
    }

    /**
     * This is called after the scene to which we are moving is guaranteed
     * to have been loaded into the server.
     */
    protected void finishMoveToRequest (
        BodyObject source, SceneManager scmgr, int sceneVersion,
        SceneMoveListener listener)
    {
        // move to the place object associated with this scene
        PlaceObject plobj = scmgr.getPlaceObject();
        int ploid = plobj.getOid();

        try {
            // try doing the actual move
            PlaceConfig config = _locprov.moveTo(source, ploid);

            // check to see if they need a newer version of the scene data
            SceneModel model = scmgr.getSceneModel();
            if (sceneVersion < model.version) {
                listener.moveSucceededPlusUpdate(ploid, config, model);
            } else {
                listener.moveSucceeded(ploid, config);
            }

        } catch (InvocationException sfe) {
            listener.requestFailed(sfe.getMessage());
        }
    }

    /**
     * Ejects the specified body from their current scene and sends them a
     * request to move to the specified new scene. This is the
     * scene-equivalent to {@link LocationProvider#moveBody}.
     */
    public void moveBody (BodyObject source, int sceneId)
    {
        // first remove them from their old place
        _locprov.leaveOccupiedPlace(source);

        // then send a forced move notification
        SceneSender.forcedMove(source, sceneId);
    }

    /** The location provider we use to handle low-level location stuff. */
    protected LocationProvider _locprov;

    /** The scene registry with which we interact. */
    protected SceneRegistry _screg;
}
