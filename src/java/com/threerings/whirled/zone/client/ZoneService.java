//
// $Id: ZoneService.java,v 1.6 2002/08/14 19:07:58 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.SceneModel;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * Defines the client interface to the zone related invocation services
 * (e.g. moving between zones).
 */
public interface ZoneService extends InvocationService
{
    public static interface ZoneMoveListener extends InvocationListener
    {
        public void moveSucceeded (
            int placeId, PlaceConfig config, ZoneSummary summary);

        public void moveSucceededPlusUpdate (
            int placeId, PlaceConfig config, ZoneSummary summary,
            SceneModel model);
    }

    /**
     * Requests that that this client's body be moved to the specified
     * scene in the specified zone.
     *
     * @param zoneId the zone id to which we want to move.
     * @param sceneId the scene id to which we want to move.
     * @param sceneVers the version number of the scene object that we
     * have in our local repository.
     * @param listener the object that will receive the callback when the
     * request succeeds or fails.
     */
    public void moveTo (Client client, int zoneId, int sceneId,
                        int sceneVers, ZoneMoveListener listener);
}
