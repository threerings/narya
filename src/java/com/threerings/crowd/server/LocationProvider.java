//
// $Id: LocationProvider.java,v 1.2 2001/08/04 01:13:36 mdb Exp $

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

        // acquire a lock on the body object to ensure that rapid fire
        // moveto requests don't break things
        if (!source.acquireLock("moveToLock")) {
            // if we're still locked, a previous moveTo request hasn't
            // been fully processed
            return createResponse("MoveFailed", "m.move_in_progress");
        }

        // find out if they were previously in some other location
        if (source.location != -1) {
            // remove them from the occupant list of the previous location
            try {
                PlaceObject pold = (PlaceObject)
                    CherServer.omgr.getObject(source.location);
                pold.removeFromOccupants(source.getOid());

            } catch (ClassCastException cce) {
                Log.warning("Body claims to be at location which " +
                            "references non-PlaceObject!? " +
                            "[boid=" + source.getOid() +
                            ", poid=" + source.location  + "].");
            }
        }

        // add the body object id to the place object's occupant list
        PlaceObject place = (PlaceObject)pobj;
        place.addToOccupants(source.getOid());

        // set their new location
        source.setLocation(place.getOid());

        // and finally queue up a lock release event to release the lock
        // once all these events are processed
        source.releaseLock("moveToLock");

        return createResponse("MoveSucceeded");
    }
}
