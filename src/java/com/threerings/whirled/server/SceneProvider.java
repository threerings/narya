//
// $Id: SceneProvider.java,v 1.1 2001/08/11 04:09:50 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.cher.util.Codes;

import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.server.LocationProvider;

import com.threerings.whirled.Log;

/**
 * The scene provider handles the server side of the scene related
 * invocation services (e.g. moving from scene to scene).
 */
public class SceneProvider extends InvocationProvider
{
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
                sendResponse(fsource, finvid, "MoveFailed",
                             "m.no_such_place");
            }
        };

        // make sure the scene they are headed to is actually loaded into
        // the server
        WhirledServer.screg.resolveScene(sceneId, rl);
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

        // try doing the actual move
        String rcode = LocationProvider.moveTo(source, ploid);

        // if the move failed, let them know
        if (!rcode.equals(Codes.SUCCESS)) {
            sendResponse(source, invid, "MoveFailed", rcode);
            return;
        }

        // otherwise check to see if they need a newer version of the
        // scene data

        sendResponse(source, invid, "MoveSucceeded", new Integer(ploid));
    }
}
