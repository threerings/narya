//
// $Id: LocationService.java,v 1.1 2001/07/23 21:14:27 mdb Exp $

package com.threerings.cocktail.party.client;

import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationManager;
import com.threerings.cocktail.party.Log;

/**
 * The location services provide a mechanism by which the client can
 * request to move from place to place in the server. These services
 * should not be used directly, but instead should be accessed via the
 * location manager.
 *
 * @see LocationManager
 */
public class LocationService
{
    /** The module name for the location services. */
    public static final String MODULE = "location";

    /**
     * Requests that that this client's body be moved to the specified
     * location.
     */
    public static void moveTo (Client client, int placeId,
                               LocationManager rsptarget)
    {
        InvocationManager invmgr = client.getInvocationManager();
        Object[] args = new Object[] { new Integer(placeId) };
        invmgr.invoke(MODULE, "MoveTo", args, rsptarget);
        Log.info("Sent moveTo request [place=" + placeId + "].");
    }
}
