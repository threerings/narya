//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

import com.threerings.crowd.data.PlaceConfig;

/**
 * The location services provide a mechanism by which the client can request to move from place to
 * place in the server. These services should not be used directly, but instead should be accessed
 * via the {@link LocationDirector}.
 */
public interface LocationService extends InvocationService<ClientObject>
{
    /**
     * Used to communicate responses to {@link LocationService#moveTo} requests.
     */
    public static interface MoveListener extends InvocationListener
    {
        /**
         * Called in response to a successful {@link LocationService#moveTo} request.
         */
        void moveSucceeded (PlaceConfig config);
    }

    /**
     * Requests that this client's body be moved to the specified location.
     *
     * @param placeId the object id of the place object to which the body should be moved.
     * @param listener the listener that will be informed of success or failure.
     */
    void moveTo (int placeId, MoveListener listener);

    /**
     * Requests that we leave our current place and move to nowhere land.
     */
    void leavePlace ();
}
