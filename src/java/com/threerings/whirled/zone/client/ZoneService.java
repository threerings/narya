//
// $Id: ZoneService.java,v 1.7 2003/02/12 07:23:32 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * Defines the client interface to the zone related invocation services
 * (e.g. moving between zones).
 */
public interface ZoneService extends InvocationService
{
    /** Used to deliver responses to {@link #moveTo} requests. */
    public static interface ZoneMoveListener extends InvocationListener
    {
        public void moveSucceeded (
            int placeId, PlaceConfig config, ZoneSummary summary);

        public void moveSucceededWithUpdates (
            int placeId, PlaceConfig config, ZoneSummary summary,
            SceneUpdate[] updates);

        public void moveSucceededWithScene (
            int placeId, PlaceConfig config, ZoneSummary summary,
            SceneModel model);
    }

    /**
     * Requests that that this client's body be moved to the specified
     * scene in the specified zone.
     *
     * @param zoneId the zone id to which we want to move.
     * @param sceneId the scene id to which we want to move.
     * @param version the version number of the scene object that we have
     * in our local repository.
     * @param listener the object that will receive the callback when the
     * request succeeds or fails.
     */
    public void moveTo (Client client, int zoneId, int sceneId,
                        int version, ZoneMoveListener listener);
}
