//
// $Id: SceneProvider.java,v 1.15 2003/02/12 07:23:31 mdb Exp $

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
import com.threerings.whirled.data.ScenedBodyObject;

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
        try {
            effectSceneMove(source, scmgr, sceneVersion, listener);
        } catch (InvocationException sfe) {
            listener.requestFailed(sfe.getMessage());
        }
    }

    /**
     * Moves the supplied body into the supplied (already resolved) scene
     * and informs the supplied listener if the move is successfuly.
     *
     * @exception InvocationException thrown if a failure occurs
     * attempting to move the user into the place associated with the
     * scene.
     */
    public void effectSceneMove (BodyObject source, SceneManager scmgr,
                                 int sceneVersion, SceneMoveListener listener)
        throws InvocationException
    {
        // move to the place object associated with this scene
        int ploid = scmgr.getPlaceObject().getOid();
        PlaceConfig config = _locprov.moveTo(source, ploid);

        // now that we've finally moved, we can update the user object
        // with the new scene id
        ((ScenedBodyObject)source).setSceneId(scmgr.getScene().getId());

        // check to see if they need a newer version of the scene data
        SceneModel model = scmgr.getScene().getSceneModel();
        if (sceneVersion < model.version) {
            listener.moveSucceededWithScene(ploid, config, model);
        } else {
            listener.moveSucceeded(ploid, config);
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

    /**
     * Ejects the specified body from their current scene and zone. This
     * is the zone equivalent to {@link
     * LocationProvider#leaveOccupiedPlace}.
     */
    public void leaveOccupiedScene (ScenedBodyObject source)
    {
        // remove them from their occupied place
        _locprov.leaveOccupiedPlace((BodyObject)source);

        // and clear out their scene information
        source.setSceneId(-1);
    }

    /** The location provider we use to handle low-level location stuff. */
    protected LocationProvider _locprov;

    /** The scene registry with which we interact. */
    protected SceneRegistry _screg;
}
