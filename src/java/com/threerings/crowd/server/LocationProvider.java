//
// $Id: LocationProvider.java,v 1.1 2001/07/23 21:14:27 mdb Exp $

package com.threerings.cocktail.party.server;

import com.threerings.cocktail.cher.dobj.DObject;
import com.threerings.cocktail.cher.server.CherServer;
import com.threerings.cocktail.cher.server.InvocationProvider;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.*;

/**
 * This class provides the server end of the location services.
 */
public class LocationProvider extends InvocationProvider
{
    /**
     * Processes a request from a client to move to a new place.
     */
    public Object[] handleMoveToRequest (BodyObject source, int placeId)
    {
        // make sure the place in question actually exists
        DObject pobj = CherServer.omgr.getObject(placeId);
        if (pobj == null || !(pobj instanceof PlaceObject)) {
            Log.info("Requested to move to non-existent place " +
                     "[source=" + source + ", place=" + placeId + "].");
            return createResponse("MoveFailed", "m.no_such_place");
        }

        // add the body object id to the place object's occupant list
        PlaceObject place = (PlaceObject)pobj;
        place.addToOccupants(source.getOid());
        return createResponse("MoveSucceeded");
    }
}
