//
// $Id: ZoneProvider.java,v 1.8 2002/05/26 02:24:46 mdb Exp $

package com.threerings.whirled.zone.server;

import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.server.SceneRegistry;
import com.threerings.whirled.server.SceneManager;

import com.threerings.whirled.zone.Log;
import com.threerings.whirled.zone.data.ZoneCodes;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.data.ZonedBodyObject;

/**
 * Provides zone related services which are presently the ability to move
 * from zone to zone.
 */
public class ZoneProvider
    extends InvocationProvider implements ZoneCodes
{
    /**
     * Constructs a zone provider that will interoperate with the supplied
     * zone and scene registries. The zone provider will automatically be
     * constructed and registered by the {@link ZoneRegistry}, which a
     * zone-using system must create and initialize in their server.
     */
    public ZoneProvider (
        InvocationManager invmgr, ZoneRegistry zonereg, SceneRegistry screg)
    {
        _invmgr = invmgr;
        _zonereg = zonereg;
        _screg = screg;
    }

    /**
     * Processes a request from a client to move to a scene in a new zone.
     *
     * @param source the user requesting the move.
     * @param invid the invocation id of the request.
     * @param zoneId the qualified zone id of the new zone.
     * @param sceneId the identifier of the new scene.
     * @param sceneVew the version of the scene model currently held by
     * the client.
     */
    public void handleMoveToRequest (BodyObject source, int invid,
                                     int zoneId, int sceneId, int sceneVer)
    {
        // look up the zone manager for the zone
        ZoneManager zmgr = _zonereg.getZoneManager(zoneId);
        if (zmgr == null) {
            Log.warning("Requested to enter a zone for which we have no " +
                        "manager [user=" + source +
                        ", zoneId=" + zoneId + "].");
            sendResponse(source, invid, MOVE_FAILED_RESPONSE, NO_SUCH_ZONE);
            return;
        }

        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = source;
        final int finvid = invid;
        final int fsceneId = sceneId;
        final int fsceneVer = sceneVer;

        // resolve the zone!
        ZoneManager.ResolutionListener zl = new ZoneManager.ResolutionListener()
        {
            public void zoneWasResolved (ZoneSummary summary) {
                continueMoveToRequest(fsource, finvid, summary,
                                      fsceneId, fsceneVer);
            }

            public void zoneFailedToResolve (int zoneId, Exception reason) {
                Log.warning("Unable to resolve zone [zoneId=" + zoneId +
                            ", reason=" + reason + "].");
                sendResponse(fsource, finvid,
                             MOVE_FAILED_RESPONSE, NO_SUCH_ZONE);
            }
        };
        zmgr.resolveZone(zoneId, zl);
    }

    /**
     * This is called after we have resolved our zone.
     */
    protected void continueMoveToRequest (
        BodyObject source, int invid, ZoneSummary summary,
        int sceneId, int sceneVer)
    {
        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = source;
        final int finvid = invid;
        final ZoneSummary fsum = summary;
        final int fsceneVer = sceneVer;

        // give the zone manager a chance to veto the request
        ZoneManager zmgr = _zonereg.getZoneManager(summary.zoneId);
        String errmsg = zmgr.ratifyBodyEntry(source, summary.zoneId);
        if (errmsg != null) {
            sendResponse(fsource, finvid, MOVE_FAILED_RESPONSE, errmsg);
            return;
        }

        // create a callback object that will handle the resolution or
        // failed resolution of the scene
        SceneRegistry.ResolutionListener rl =
            new SceneRegistry.ResolutionListener()
        {
            public void sceneWasResolved (SceneManager scmgr) {
                finishMoveToRequest(fsource, finvid, fsum, scmgr, fsceneVer);
            }

            public void sceneFailedToResolve (int sceneId, Exception reason) {
                Log.warning("Unable to resolve scene [sceneid=" + sceneId +
                            ", reason=" + reason + "].");
                // pretend like the scene doesn't exist to the client
                sendResponse(fsource, finvid,
                             MOVE_FAILED_RESPONSE, NO_SUCH_PLACE);
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
        BodyObject source, int invid, ZoneSummary summary,
        SceneManager scmgr, int sceneVersion)
    {
        // move to the place object associated with this scene
        PlaceObject plobj = scmgr.getPlaceObject();
        int ploid = plobj.getOid();

        try {
            // try doing the actual move
            PlaceConfig config = LocationProvider.moveTo(source, ploid);

            // now that we've finally moved, we can update the user object
            // with the new zone id
            ((ZonedBodyObject)source).setZoneId(summary.zoneId);

            // check to see if they need a newer version of the scene data
            SceneModel model = scmgr.getSceneModel();
            if (sceneVersion < model.version) {
                // then send the moveTo response
                sendResponse(source, invid, MOVE_SUCCEEDED_PLUS_UPDATE_RESPONSE,
                             new Object[] { new Integer(ploid), config,
                                            summary, model });

            } else {
                // then send the moveTo response
                sendResponse(source, invid, MOVE_SUCCEEDED_RESPONSE,
                             new Integer(ploid), config, summary);
            }

            // let the zone manager know that someone just came on in
            ZoneManager zmgr = _zonereg.getZoneManager(summary.zoneId);
            zmgr.bodyDidEnterZone(source, summary.zoneId);

        } catch (ServiceFailedException sfe) {
            sendResponse(source, invid,
                         MOVE_FAILED_RESPONSE, sfe.getMessage());
        }
    }

    /**
     * Ejects the specified body from their current scene and sends them a
     * request to move to the specified new zone and scene. This is the
     * zone-equivalent to {@link LocationProvider#moveBody}.
     */
    public void moveBody (BodyObject source, int zoneId, int sceneId)
    {
        // first remove them from their old place
        LocationProvider.leaveOccupiedPlace(source);

        // then send a move notification
        _invmgr.sendNotification(
            source.getOid(), MODULE_NAME, MOVE_NOTIFICATION,
            new Object[] { new Integer(zoneId), new Integer(sceneId) });
    }

    /** The invocation manager with which we interact. */
    protected InvocationManager _invmgr;

    /** The zone registry with which we communicate. */
    protected ZoneRegistry _zonereg;

    /** The scene registry with which we communicate. */
    protected SceneRegistry _screg;
}
