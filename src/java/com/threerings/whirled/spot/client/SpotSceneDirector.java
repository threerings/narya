//
// $Id: SpotSceneDirector.java,v 1.4 2001/12/16 05:18:20 mdb Exp $

package com.threerings.whirled.spot.client;

import java.util.Iterator;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;

/**
 * Extends the standard scene director with facilities to move between
 * locations within a scene.
 */
public class SpotSceneDirector
    implements SpotCodes
{
    /**
     * This is used to communicate back to the caller of {@link
     * #changeLocation}.
     */
    public static interface ChangeObserver
    {
        /**
         * Indicates that the requested location change succeeded.
         */
        public void locationChangeSucceeded (int locationId);

        /**
         * Indicates that the requested location change failed and
         * provides a reason code explaining the failure.
         */
        public void locationChangeFailed (int locationId, String reason);
    }

    /**
     * Creates a new spot scene director with the specified context and
     * which will cooperate with the supplied scene director.
     *
     * @param ctx the active client context.
     * @param scdir the scene director with which we will be cooperating.
     */
    public SpotSceneDirector (WhirledContext ctx, SceneDirector scdir)
    {
        _ctx = ctx;
        _scdir = scdir;
    }

    /**
     * Requests that this client move to the location specified by the
     * supplied portal id. A request will be made and when the response is
     * received, the location observers will be notified of success or
     * failure.
     */
    public void traversePortal (int portalId)
    {
        // look up the destination scene and location
        DisplaySpotScene scene = (DisplaySpotScene)_scdir.getScene();
        if (scene == null) {
            Log.warning("Requested to traverse portal when we have " +
                        "no scene [portalId=" + portalId + "].");
            return;
        }

        // find the portal they're talking about
        int targetSceneId = -1, targetLocId = -1;
        Iterator portals = scene.getPortals().iterator();
        while (portals.hasNext()) {
            Portal portal = (Portal)portals.next();
            if (portal.locationId == portalId) {
                targetSceneId = portal.targetSceneId;
                targetLocId = portal.targetLocId;
            }
        }

        // make sure we found the portal
        if (targetSceneId == -1) {
            portals = scene.getPortals().iterator();
            Log.warning("Requested to traverse non-existent portal " +
                        "[portalId=" + portalId +
                        ", portals=" + StringUtil.toString(portals) + "].");
        }

        // prepare to move to this scene (sets up pending data)
        if (!_scdir.prepareMoveTo(targetSceneId)) {
            return;
        }

        // check the version of our cached copy of the scene to which
        // we're requesting to move; if we were unable to load it, assume
        // a cached version of zero
        int sceneVer = 0;
        SceneModel pendingModel = _scdir.getPendingModel();
        if (pendingModel != null) {
            sceneVer = pendingModel.version;
        }

        // issue a traversePortal request
        SpotService.traversePortal(
            _ctx.getClient(), scene.getId(), portalId, sceneVer, _scdir);
    }

    /**
     * Issues a request to change our location within the scene to the
     * location identified by the specified id. Most client entities find
     * out about location changes via changes to the occupant info data,
     * but the initiator of a location change request can be notified of
     * its success or failure, primarily so that it can act in
     * anticipation of a successful location change (like by starting a
     * sprite moving toward the new location), but backtrack if it finds
     * out that the location change failed.
     */
    public void changeLocation (int locationId, ChangeObserver obs)
    {
        // refuse if there's a pending location change
        if (_pendingLocId != -1) {
            return;
        }

        // make sure we're currently in a scene
        DisplaySpotScene scene = (DisplaySpotScene)_scdir.getScene();
        if (scene == null) {
            Log.warning("Requested to change locations, but we're not " +
                        "currently in any scene [locId=" + locationId + "].");
            return;
        }

        // make sure the specified location is in the current scene
        int locidx = -1;
        Iterator locs = scene.getLocations().iterator();
        for (int i = 0; locs.hasNext(); i++) {
            Location loc = (Location)locs.next();
            if (loc.locationId == locationId) {
                locidx = i;
                break;
            }
        }
        if (locidx == -1) {
            Log.warning("Requested to change to a location that's not " +
                        "in the current scene [locs=" + StringUtil.toString(
                            scene.getLocations().iterator()) +
                        ", locId=" + locationId + "].");
            return;
        }

        // make a note that we're changing to this location
        _pendingLocId = locationId;
        _changeObserver = obs;
        // and send the location change request
        SpotService.changeLoc(_ctx.getClient(), scene.getId(),
                              locationId, this);
    }

    /**
     * Called in response to a successful <code>changeLoc</code> request.
     */
    public void handleChangeLocSucceeded (int invid)
    {
        ChangeObserver obs = _changeObserver;
        int locId = _pendingLocId;

        // clear out our pending location info
        _pendingLocId = -1;
        _changeObserver = null;

        // if we had an observer, let them know things went well
        if (obs != null) {
            obs.locationChangeSucceeded(locId);
        }
    }

    /**
     * Called in response to a failed <code>changeLoc</code> request.
     */
    public void handleChangeLocFailed (int invid, String reason)
    {
        ChangeObserver obs = _changeObserver;
        int locId = _pendingLocId;

        // clear out our pending location info
        _pendingLocId = -1;
        _changeObserver = null;

        // if we had an observer, let them know things went well
        if (obs != null) {
            obs.locationChangeFailed(locId, reason);
        }
    }

    /** The active client context. */
    protected WhirledContext _ctx;

    /** The scene director with which we are cooperating. */
    protected SceneDirector _scdir;

    /** The location id on which we have an outstanding change location
     * request. */
    protected int _pendingLocId = -1;

    /** An entity that wants to know if a requested location change
     * succeded or failed. */
    protected ChangeObserver _changeObserver;
}
