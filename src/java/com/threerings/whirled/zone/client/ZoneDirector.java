//
// $Id: ZoneDirector.java,v 1.1 2001/12/04 00:31:58 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.client.DisplaySceneFactory;
import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.client.persist.SceneRepository;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * The zone director extends the scene director with the notion of zones.
 * Zones are self-contained, connected groups of scenes. The normal scene
 * director services can be used to move from scene to scene, but moving
 * to a new zone requires a special move request which can be accomplished
 * via the zone director. The zone director also makes available the zone
 * summary which provides information on the zone which can be used to
 * generate an overview map or similar.
 */
public class ZoneDirector extends SceneDirector
{
    /**
     * Constructs a zone director with the supplied context, repository
     * and scene factory. A zone director is required on the client side
     * for systems that wish to use the zone services.
     */
    public ZoneDirector (WhirledContext ctx, SceneRepository screp,
                         DisplaySceneFactory dsfact)
    {
        super(ctx, screp, dsfact);
    }

    /**
     * Returns the summary for the zone currently occupied by the client
     * or null if the client does not currently occupy a zone (not a
     * normal situation).
     */
    public ZoneSummary getZoneSummary ()
    {
        return _summary;
    }

    /**
     * Requests that this client move the specified scene in the specified
     * zone. A request will be made and when the response is received, the
     * location observers will be notified of success or failure.
     */
    public void moveTo (int zoneId, int sceneId)
    {
        // if the requested zone is the same as our current zone, we just
        // want a regular old moveTo request
        if (_summary != null && zoneId == _summary.zoneId) {
            moveTo(sceneId);

        } else { // otherwise, we make a zoned moveTo request
            // prepare to move to this scene (sets up pending data)
            if (!prepareMoveTo(sceneId)) {
                return;
            }

            // check the version of our cached copy of the scene to which
            // we're requesting to move; if we were unable to load it, assume
            // a cached version of zero
            int sceneVers = 0;
            if (_pendingModel != null) {
                sceneVers = _pendingModel.version;
            }

            // issue a moveTo request
            ZoneService.moveTo(_ctx.getClient(), zoneId,
                               sceneId, sceneVers, this);
        }
    }

    /**
     * Called in response to a successful zoned <code>moveTo</code>
     * request.
     */
    public void handleMoveSucceeded (
        int invid, int placeId, PlaceConfig config, ZoneSummary summary)
    {
        // keep track of the summary
        _summary = summary;

        // and pass the rest off to the standard scene transition code
        handleMoveSucceeded(invid, placeId, config);
    }

    /**
     * Called in response to a successful zoned <code>moveTo</code>
     * request when our cached scene was out of date and the server
     * determined that we needed an updated copy.
     */
    public void handleMoveSucceededPlusUpdate (
        int invid, int placeId, PlaceConfig config, ZoneSummary summary,
        SceneModel model)
    {
        // keep track of the summary
        _summary = summary;

        // and pass the rest off to the standard scene transition code
        handleMoveSucceededPlusUpdate(invid, placeId, config, model);
    }

    /** A reference to the zone summary for the currently occupied
     * zone. */
    protected ZoneSummary _summary;
}
