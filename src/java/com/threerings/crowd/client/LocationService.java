//
// $Id: LocationService.java,v 1.8 2004/02/25 14:41:47 mdb Exp $

package com.threerings.crowd.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.PlaceConfig;

/**
 * The location services provide a mechanism by which the client can
 * request to move from place to place in the server. These services
 * should not be used directly, but instead should be accessed via the
 * {@link LocationDirector}.
 */
public interface LocationService extends InvocationService
{
    /**
     * Used to communicate responses to {@link #moveTo} requests.
     */
    public static interface MoveListener extends InvocationListener
    {
        /**
         * Called in response to a successful {@link #moveTo} request.
         */
        public void moveSucceeded (PlaceConfig config);
    }

    /**
     * Requests that this client's body be moved to the specified
     * location.
     *
     * @param client a reference to the client object that defines the
     * context in which this invocation service should be executed.
     * @param placeId the object id of the place object to which the body
     * should be moved.
     * @param listener the listener that will be informed of success or
     * failure.
     */
    public void moveTo (Client client, int placeId, MoveListener listener);
}
