//
// $Id: LocationProvider.java,v 1.7 2001/08/20 20:54:57 mdb Exp $

package com.threerings.cocktail.party.server;

import com.threerings.cocktail.cher.dobj.DObject;
import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.cher.util.Codes;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.*;
import com.threerings.cocktail.party.server.PartyServer;

/**
 * This class provides the server end of the location services.
 */
public class LocationProvider extends InvocationProvider
{
    /**
     * Processes a request from a client to move to a new place.
     */
    public void handleMoveToRequest (BodyObject source, int invid,
                                     int placeId)
    {
        // try to do the actual move
        String rcode = moveTo(source, placeId);

        // send the response
        if (rcode.equals(Codes.SUCCESS)) {
            sendResponse(source, invid, "MoveSucceeded");
        } else {
            sendResponse(source, invid, "MoveFailed", rcode);
        }
    }

    /**
     * Moves the specified body from whatever location they currently
     * occupy to the location identified by the supplied place id.
     *
     * @return the string <code>Codes.SUCCESS</code> if the move was
     * successful or a reason code for failure if not.
     */
    public static String moveTo (BodyObject source, int placeId)
    {
        int bodoid = source.getOid();

        // make sure the place in question actually exists
        PlaceManager pmgr = PartyServer.plreg.getPlaceManager(placeId);
        if (pmgr == null) {
            Log.info("Requested to move to non-existent place " +
                     "[source=" + source + ", place=" + placeId + "].");
            return "m.no_such_place";
        }

        // acquire a lock on the body object to ensure that rapid fire
        // moveto requests don't break things
        if (!source.acquireLock("moveToLock")) {
            // if we're still locked, a previous moveTo request hasn't
            // been fully processed
            return "m.move_in_progress";
        }

        // make sure they're not already in the location they're asking to
        // move to
        if (source.location == placeId) {
            return "m.already_there";
        }

        // find out if they were previously in some other location
        if (source.location != -1) {
            // remove them from the occupant list of the previous location
            try {
                PlaceObject pold = (PlaceObject)
                    PartyServer.omgr.getObject(source.location);
                if (pold != null) {
                    Object key = new Integer(bodoid);
                    // remove their occupant info (which is keyed on oid)
                    pold.removeFromOccupantInfo(key);
                    // and remove them from the occupant list
                    pold.removeFromOccupants(bodoid);

                } else {
                    Log.info("Body's prior location no longer around? " +
                             "[boid=" + bodoid +
                             ", poid=" + source.location + "].");
                }

            } catch (ClassCastException cce) {
                Log.warning("Body claims to be at location which " +
                            "references non-PlaceObject!? " +
                            "[boid=" + bodoid +
                            ", poid=" + source.location  + "].");
            }
        }

        // set the body's new location
        PlaceObject place = pmgr.getPlaceObject();
        source.setLocation(place.getOid());

        // generate a new occupant info record and add it to the target
        // location
        OccupantInfo info = pmgr.buildOccupantInfo(source);
        if (info != null) {
            place.addToOccupantInfo(info);
        }

        // add the body object id to the place object's occupant list
        place.addToOccupants(bodoid);

        // and finally queue up a lock release event to release the lock
        // once all these events are processed
        source.releaseLock("moveToLock");

        return Codes.SUCCESS;
    }
}
