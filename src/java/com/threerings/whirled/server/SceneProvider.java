//
// $Id: SceneProvider.java,v 1.7 2001/12/16 05:39:16 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.SceneCodes;
import com.threerings.whirled.data.SceneModel;

/**
 * The scene provider handles the server side of the scene related
 * invocation services (e.g. moving from scene to scene).
 */
public class SceneProvider extends InvocationProvider
    implements SceneCodes
{
    /**
     * Constructs a scene provider that will interact with the supplied
     * scene registry.
     */
    public SceneProvider (SceneRegistry screg)
    {
        _screg = screg;
    }

    /**
     * Processes a request from a client to move to a new scene.
     */
    public void handleMoveToRequest (BodyObject source, int invid,
                                     int sceneId, int sceneVersion)
    {
        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = source;
        final int finvid = invid;
        final int fsceneVer = sceneVersion;

        // create a callback object that will handle the resolution or
        // failed resolution of the scene
        SceneRegistry.ResolutionListener rl =
            new SceneRegistry.ResolutionListener()
        {
            public void sceneWasResolved (SceneManager scmgr)
            {
                finishMoveToRequest(fsource, finvid, scmgr, fsceneVer);
            }

            public void sceneFailedToResolve (
                int rsceneId, Exception reason)
            {
                Log.warning("Unable to resolve scene [sceneid=" + rsceneId +
                            ", reason=" + reason + "].");
                // pretend like the scene doesn't exist to the client
                sendResponse(fsource, finvid, MOVE_FAILED_RESPONSE,
                             NO_SUCH_PLACE);
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
    protected void finishMoveToRequest (BodyObject source, int invid,
                                        SceneManager scmgr, int sceneVersion)
    {
        // move to the place object associated with this scene
        PlaceObject plobj = scmgr.getPlaceObject();
        int ploid = plobj.getOid();

        try {
            // try doing the actual move
            PlaceConfig config = LocationProvider.moveTo(source, ploid);

            // check to see if they need a newer version of the scene data
            SceneModel model = scmgr.getSceneModel();
            if (sceneVersion < model.version) {
                // then send the moveTo response
                sendResponse(source, invid, MOVE_SUCCEEDED_PLUS_UPDATE_RESPONSE,
                             new Integer(ploid), config, model);

            } else {
                // then send the moveTo response
                sendResponse(source, invid, MOVE_SUCCEEDED_RESPONSE,
                             new Integer(ploid), config);
            }

        } catch (ServiceFailedException sfe) {
            sendResponse(source, invid, MOVE_FAILED_RESPONSE, sfe.getMessage());
        }
    }

    /** The scene registry with which we interact. */
    protected SceneRegistry _screg;
}
