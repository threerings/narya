//
// $Id: LocationService.java,v 1.5 2002/04/15 16:28:01 shaper Exp $

package com.threerings.crowd.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;
import com.threerings.crowd.Log;
import com.threerings.crowd.data.LocationCodes;

/**
 * The location services provide a mechanism by which the client can
 * request to move from place to place in the server. These services
 * should not be used directly, but instead should be accessed via the
 * location director.
 *
 * @see LocationDirector
 */
public class LocationService implements LocationCodes
{
    /**
     * Requests that that this client's body be moved to the specified
     * location.
     */
    public static void moveTo (Client client, int placeId,
                               LocationDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(placeId) };
        invdir.invoke(MODULE_NAME, MOVE_TO_REQUEST, args, rsptarget);
        Log.info("Sent moveTo request [place=" + placeId + "].");
    }
}
