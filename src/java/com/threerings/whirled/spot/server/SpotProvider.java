//
// $Id: SpotProvider.java,v 1.5 2001/12/16 21:02:18 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneRegistry;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.client.SpotCodes;
import com.threerings.whirled.spot.data.SpotOccupantInfo;

/**
 * This class provides the server side of the spot services.
 */
public class SpotProvider extends InvocationProvider
    implements SpotCodes
{
    /**
     * Constructs a spot provider and registers it with the invocation
     * manager to handle spot services. This need be done by a server that
     * wishes to make use of the Spot services.
     */
    public static void init (
        InvocationManager invmgr, SceneRegistry screg, RootDObjectManager omgr)
    {
        // we'll need these later
        _screg = screg;
        _omgr = omgr;

        // register a spot provider instance
        invmgr.registerProvider(MODULE_NAME, new SpotProvider());
    }

    /**
     * Processes a request from a client to traverse a portal.
     *
     * @param source the body object of the client making the request.
     * @param invid the invocation service invocation id.
     * @param sceneId the source scene id.
     * @param portalId the portal in the source scene that is being
     * traversed.
     * @param sceneVer the version of the destination scene data that the
     * client has cached.
     */
    public void handleTraversePortalRequest (
        BodyObject source, int invid, int sceneId, int portalId, int sceneVer)
    {
        try {
            // obtain the source scene
            SpotSceneManager smgr = (SpotSceneManager)
                _screg.getSceneManager(sceneId);
            if (smgr == null) {
                Log.warning("Traverse portal missing source scene " +
                            "[user=" + source.username +
                            ", sceneId=" + sceneId +
                            ", portalId=" + portalId + "].");
                throw new ServiceFailedException(INTERNAL_ERROR);
            }

            // obtain the destination scene and location id
            RuntimeSpotScene rss = (RuntimeSpotScene)smgr.getScene();
            int destSceneId = rss.getTargetSceneId(portalId);
            final int destLocId = rss.getTargetLocationId(portalId);

            // make sure this portal has valid info
            if (destSceneId == -1) {
                Log.warning("Traverse portal provided with invalid portal " +
                            "[user=" + source.username +
                            ", sceneId=" + sceneId +
                            ", portalId=" + portalId +
                            ", destSceneId=" + destSceneId + "].");
                throw new ServiceFailedException(NO_SUCH_PORTAL);
            }

            // avoid cluttering up the method declaration with final
            // keywords
            final BodyObject fsource = source;
            final int finvid = invid;
            final int fportalId = portalId;
            final int fsceneVer = sceneVer;

            // create a callback object that will handle the resolution or
            // failed resolution of the scene
            SceneRegistry.ResolutionListener rl =
                new SceneRegistry.ResolutionListener() {
                    public void sceneWasResolved (SceneManager scmgr) {
                        SpotSceneManager sscmgr = (SpotSceneManager)scmgr;
                        finishTraversePortalRequest(
                            fsource, finvid, sscmgr, fsceneVer,
                            fportalId, destLocId);
                    }

                    public void sceneFailedToResolve (
                        int rsceneId, Exception reason) {
                        Log.warning("Unable to resolve target scene " +
                                    "[sceneId=" + rsceneId +
                                    ", reason=" + reason + "].");
                        // pretend like the scene doesn't exist to the client
                        sendResponse(fsource, finvid, MOVE_FAILED_RESPONSE,
                                     NO_SUCH_PLACE);
                    }
                };

            // make sure the scene they are headed to is actually loaded into
            // the server
            _screg.resolveScene(destSceneId, rl);

        } catch (ServiceFailedException sfe) {
            sendResponse(source, invid, MOVE_FAILED_RESPONSE, sfe.getMessage());
        }
    }

    /**
     * This is called after the scene to which we are moving is guaranteed
     * to have been loaded into the server.
     */
    protected void finishTraversePortalRequest (
        BodyObject source, int invid, SpotSceneManager scmgr,
        int sceneVer, int exitPortalId, int destLocId)
    {
        // move to the place object associated with this scene
        PlaceObject plobj = scmgr.getPlaceObject();
        int ploid = plobj.getOid();
        int bodyOid = source.getOid();

        // if they were in a scene (and at a location) prior to issuing
        // this traverse portal request, we need to send a notification to
        // that scene indicating that they are headed to the portal from
        // which they depart. we unfortunately can't do it deep in the
        // bowels of LocationProvider.moveTo() which is where we'd like to
        // do it to ensure that nothing else ran amuck in the process.
        // since we can't, we simply send another response putting the
        // user back where they were in the event that anything fails
        // during the moveTo process. it's a hack, but it's better than
        // ripping apart moveTo and restructuring the code with this
        // requirement in mind
        int oldLocId =
            updateLocation(source.location, bodyOid, exitPortalId);

        // let the destination scene manager know that we're coming in
        scmgr.mapEnteringBody(bodyOid, destLocId);

        try {
            // try doing the actual move
            PlaceConfig config = LocationProvider.moveTo(source, ploid);

            // check to see if they need a newer version of the scene data
            SceneModel model = scmgr.getSceneModel();
            if (sceneVer < model.version) {
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

            // we need to undo the move to the exit portal location that
            // we enacted earlier
            updateLocation(source.location, bodyOid, oldLocId);

            // and let the destination scene manager know that we're no
            // longer coming in
            scmgr.clearEnteringBody(bodyOid);
        }
    }

    /**
     * Processes a request from a client to move to a new location.
     */
    public void handleChangeLocRequest (
        BodyObject source, int invid, int sceneId, int locationId)
    {
        try {
            // look up the scene manager for the specified scene
            SpotSceneManager smgr = (SpotSceneManager)
                _screg.getSceneManager(sceneId);
            if (smgr == null) {
                Log.warning("User requested to change location in " +
                            "non-existent scene [user=" + source.username +
                            ", sceneId=" + sceneId +
                            ", locId=" + locationId + "].");
                throw new ServiceFailedException(INTERNAL_ERROR);
            }

            int locOid = smgr.handleChangeLocRequest(source, locationId);
            sendResponse(source, invid, CHANGE_LOC_SUCCEEDED_RESPONSE,
                         new Integer(locOid));

        } catch (ServiceFailedException sfe) {
            sendResponse(source, invid, CHANGE_LOC_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /**
     * Handles {@link SpotCodes#CLUSTER_SPEAK_REQUEST} messages.
     */
    public void handleClusterSpeakRequest (
        BodyObject source, int invid, int sceneId, int locId, String message)
    {
        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("User requested cluster chat in " +
                        "non-existent scene [user=" + source.username +
                        ", sceneId=" + sceneId + ", locId=" + locId +
                        ", message=" + message + "].");

        } else {
            // pass this request on to the spot scene manager as it will
            // need to check that the location exists and that the
            // requester occupies it and so on
            smgr.handleClusterSpeakRequest(source, locId, message);
        }
    }

    /**
     * Looks up the specified place object, obtains the occupant info for
     * the specified body object and updates the location id in said
     * occupant info.
     *
     * @return old location id or -1 if no update took place (because
     * something was not in order, like the place object didn't exist,
     * etc.).
     */
    protected static int updateLocation (
        int placeOid, int bodyOid, int locationId)
    {
        PlaceObject place = null;
        int oldLocId = -1;

        if (placeOid != -1 && locationId != -1) {
            place = (PlaceObject)_omgr.getObject(placeOid);
        }

        if (place != null) {
            Integer key = new Integer(bodyOid);
            SpotOccupantInfo info = (SpotOccupantInfo)
                place.occupantInfo.get(key);
            if (info != null) {
                oldLocId = info.locationId;
                // we need to clone the info because moveTo() down below
                // is going to update the actual info record and post it
                // in its own event, which would mess us up if we were
                // using the real thing
                info = (SpotOccupantInfo)info.clone();
                info.locationId = locationId;
                place.updateOccupantInfo(info);
            }
        }

        return oldLocId;
    }

    /** The scene registry with which we interoperate. */
    protected static SceneRegistry _screg;

    /** The object manager we use to do dobject stuff. */
    protected static RootDObjectManager _omgr;
}
