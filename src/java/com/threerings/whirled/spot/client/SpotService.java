//
// $Id: SpotService.java,v 1.13 2003/02/13 21:55:22 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.spot.data.Location;

/**
 * Defines the mechanism by which the client can request to move around
 * within a scene and between scenes (taking exit and entry locations into
 * account). These services should not be used directly, but instead
 * should be accessed via the {@link SpotSceneDirector}.
 */
public interface SpotService extends InvocationService
{
    /**
     * Requests to traverse the specified portal.
     *
     * @param portalId the portal to be traversed.
     * @param descSceneVer the version of the destination scene data that
     * the client has in its local repository.
     */
    public void traversePortal (
        Client client, int portalId, int destSceneVer,
        SceneMoveListener listener);

    /**
     * Used to communicate responses to a {@link #changeLoc} request.
     */
    public static interface ChangeLocListener extends InvocationListener
    {
        /**
         * Called when the change location request succeeds.
         */
        public void changeLocSucceeded ();
    }

    /**
     * Requests that this client's body be made to move to the specified
     * location.
     *
     * @param loc the location to which to move.
     * @param cluster if -1, the calling user will be removed from any
     * cluster they currently occupy and not made to occupy a new cluster;
     * if the bodyOid of another user, the calling user will be made to
     * join the target user's cluster, or create a cluster with the target
     * user if they are not already in one.
     */
    public void changeLoc (Client client, Location loc, int cluster,
                           ChangeLocListener listener);

    /**
     * Requests that the supplied message be delivered to listeners in the
     * cluster to which the specified location belongs.
     *
     * @param message the text of the message to be spoken.
     * @param mode an associated mode constant that can be used to
     * identify different kinds of "speech" (emote, thought bubble, etc.).
     */
    public void clusterSpeak (Client client, String message, byte mode);
}
