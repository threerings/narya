//
// $Id: SpotService.java,v 1.11 2002/08/14 19:07:57 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.whirled.client.SceneService.SceneMoveListener;

/**
 * Defines the mechanism by which the client can request to move between
 * locations within a scene and between scenes (taking exit and entry
 * locations into account). These services should not be used directly,
 * but instead should be accessed via the {@link SpotSceneDirector}.
 */
public interface SpotService extends InvocationService
{
    /**
     * Requests to traverse the specified portal.
     */
    public void traversePortal (
        Client client, int sceneId, int portalId, int sceneVer,
        SceneMoveListener listener);

    /**
     * Used to communicate responses to a {@link #changeLoc} request.
     */
    public static interface ChangeLocListener extends InvocationListener
    {
        /**
         * Called when the change location request succeeded.
         *
         * @param clusterOid the object id of the cluster object
         * associated with the new location.
         */
        public void changeLocSucceeded (int clusterOid);
    }

    /**
     * Requests that this client's body be made to occupy the specified
     * location.
     */
    public void changeLoc (Client client, int sceneId, int locationId,
                           ChangeLocListener listener);

    /**
     * Requests that the supplied message be delivered to listeners in the
     * cluster to which the specified location belongs.
     */
    public void clusterSpeak (Client client, int sceneId, int locationId,
                              String message, byte mode);
}
