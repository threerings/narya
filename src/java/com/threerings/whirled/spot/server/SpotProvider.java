//
// $Id: SpotProvider.java,v 1.13 2002/08/14 19:07:58 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneRegistry;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.client.SpotService.ChangeLocListener;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotOccupantInfo;

/**
 * Provides the server-side implementation of the spot services.
 */
public class SpotProvider
    implements SpotCodes, InvocationProvider
{
    /**
     * Creates a spot provider that can be registered with the invocation
     * manager to handle spot services.
     */
    public SpotProvider (RootDObjectManager omgr, PlaceRegistry plreg,
                         SceneRegistry screg)
    {
        // we'll need these later
        _plreg = plreg;
        _screg = screg;
        _omgr = omgr;
    }

    /**
     * Processes a request from a client to traverse a portal.
     *
     * @param source the body object of the client making the request.
     * @param sceneId the source scene id.
     * @param portalId the portal in the source scene that is being
     * traversed.
     * @param sceneVer the version of the destination scene data that the
     * client has cached.
     * @param listener the entity to which we communicate our response.
     */
    public void traversePortal (ClientObject caller, int sceneId, int portalId,
                                int sceneVer, SceneMoveListener listener)
        throws InvocationException
    {
        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = (BodyObject)caller;
        final int fportalId = portalId;
        final int fsceneVer = sceneVer;
        final SceneMoveListener flistener = listener;

        // obtain the source scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("Traverse portal missing source scene " +
                        "[user=" + fsource.who() + ", sceneId=" + sceneId +
                        ", portalId=" + portalId + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // obtain the destination scene and location id
        RuntimeSpotScene rss = (RuntimeSpotScene)smgr.getScene();
        int destSceneId = rss.getTargetSceneId(portalId);
        final int destLocId = rss.getTargetLocationId(portalId);

        // make sure this portal has valid info
        if (destSceneId == -1) {
            Log.warning("Traverse portal provided with invalid portal " +
                        "[user=" + fsource.who() + ", sceneId=" + sceneId +
                        ", portalId=" + portalId +
                        ", destSceneId=" + destSceneId + "].");
            throw new InvocationException(NO_SUCH_PORTAL);
        }

        // create a callback object that will handle the resolution or
        // failed resolution of the scene
        SceneRegistry.ResolutionListener rl =
            new SceneRegistry.ResolutionListener() {
                public void sceneWasResolved (SceneManager scmgr) {
                    SpotSceneManager sscmgr = (SpotSceneManager)scmgr;
                    finishTraversePortalRequest(
                        fsource, sscmgr, fsceneVer, fportalId, destLocId,
                        flistener);
                }

                public void sceneFailedToResolve (
                    int rsceneId, Exception reason) {
                    Log.warning("Unable to resolve target scene " +
                                "[sceneId=" + rsceneId +
                                ", reason=" + reason + "].");
                    // pretend like the scene doesn't exist to the client
                    flistener.requestFailed(NO_SUCH_PLACE);
                }
            };

        // make sure the scene they are headed to is actually loaded into
        // the server
        _screg.resolveScene(destSceneId, rl);
    }

    /**
     * This is called after the scene to which we are moving is guaranteed
     * to have been loaded into the server.
     */
    protected void finishTraversePortalRequest (
        BodyObject source, SpotSceneManager scmgr, int sceneVer,
        int exitPortalId, int destLocId, SceneMoveListener listener)
    {
        // move to the place object associated with this scene
        PlaceObject plobj = scmgr.getPlaceObject();
        int ploid = plobj.getOid();
        int bodyOid = source.getOid();

        // let the destination scene manager know that we're coming in
        scmgr.mapEnteringBody(bodyOid, destLocId);

        try {
            // try doing the actual move
            PlaceConfig config = _plreg.locprov.moveTo(source, ploid);

            // check to see if they need a newer version of the scene data
            SceneModel model = scmgr.getSceneModel();
            if (sceneVer < model.version) {
                // then send the moveTo response
                listener.moveSucceededPlusUpdate(ploid, config, model);

            } else {
                // then send the moveTo response
                listener.moveSucceeded(ploid, config);
            }

        } catch (InvocationException sfe) {
            listener.requestFailed(sfe.getMessage());

            // and let the destination scene manager know that we're no
            // longer coming in
            scmgr.clearEnteringBody(bodyOid);
        }
    }

    /**
     * Processes a request from a client to move to a new location.
     */
    public void changeLoc (ClientObject caller, int sceneId, int locationId,
                           ChangeLocListener listener)
        throws InvocationException
    {
        BodyObject source = (BodyObject)caller;

        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("User requested to change location in " +
                        "non-existent scene [user=" + source.who() +
                        ", sceneId=" + sceneId +
                        ", locId=" + locationId + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        int locOid = smgr.handleChangeLocRequest(source, locationId);
        listener.changeLocSucceeded(locOid);
    }

    /**
     * Handles request to generate a speak message in the specified cluster.
     */
    public void clusterSpeak (ClientObject caller, int sceneId, int locationId,
                              String message, byte mode)
    {
        BodyObject source = (BodyObject)caller;
        sendClusterChatMessage(sceneId, locationId, source.getOid(),
                               source.username, null, message, mode);
    }

    /**
     * Sends a cluster chat notification to the specified location in the
     * specified place object originating with the specified speaker (the
     * speaker can be a server entity that wishes to fake a "speak"
     * message, in which case the bundle argument should be non-null and
     * should contain the id of the bundle to be used to translate the
     * message text) and with the supplied message content.
     *
     * @param sceneId the scene id in which to deliver the chat message.
     * @param locId the location whose cluster will be spoken to.
     * @param speakerOid the body object id of the speaker (used to verify
     * that they are in the cluster in question).
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle the bundle identifier that will be used by the client
     * to translate the message text (or null if the message originated
     * from a real live human who wrote it in their native tongue).
     * @param message the text of the chat message.
     */
    public void sendClusterChatMessage (
        int sceneId, int locId, int speakerOid, String speaker,
        String bundle, String message, byte mode)
    {
        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("User requested cluster chat in " +
                        "non-existent scene [user=" + speaker +
                        ", sceneId=" + sceneId + ", locId=" + locId +
                        ", message=" + message + "].");

        } else {
            // pass this request on to the spot scene manager as it will
            // need to check that the location exists and that the speaker
            // occupies it and so on
            smgr.handleClusterSpeakRequest(
                speakerOid, speaker, locId, bundle, message, mode);
        }
    }

    /** The place registry with which we interoperate. */
    protected PlaceRegistry _plreg;

    /** The scene registry with which we interoperate. */
    protected SceneRegistry _screg;

    /** The object manager we use to do dobject stuff. */
    protected RootDObjectManager _omgr;
}
