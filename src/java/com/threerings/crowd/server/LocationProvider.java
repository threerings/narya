//
// $Id: LocationProvider.java,v 1.15 2002/06/20 22:38:58 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.RootDObjectManager;

import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.LocationCodes;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

/**
 * This class provides the server end of the location services.
 */
public class LocationProvider extends InvocationProvider
    implements LocationCodes
{
    /**
     * Constructs a location provider and registers it with the invocation
     * manager to handle location services. This is done automatically by
     * the {@link PlaceRegistry}.
     */
    public static void init (
        InvocationManager invmgr, RootDObjectManager omgr, PlaceRegistry plreg)
    {
        // we'll need these later
        _invmgr = invmgr;
        _omgr = omgr;
        _plreg = plreg;

        // register a location provider instance
        invmgr.registerProvider(MODULE_NAME, new LocationProvider());
    }

    /**
     * Processes a request from a client to move to a new place.
     */
    public void handleMoveToRequest (
        BodyObject source, int invid, int placeId)
    {
        try {
            // do the move
            PlaceConfig config = moveTo(source, placeId);
            // and send the response
            sendResponse(source, invid, MOVE_SUCCEEDED_RESPONSE, config);

        } catch (ServiceFailedException sfe) {
            sendResponse(source, invid, MOVE_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /**
     * Moves the specified body from whatever location they currently
     * occupy to the location identified by the supplied place id.
     *
     * @return the config object for the new location.
     *
     * @exception ServiceFaildException thrown if the move was not
     * successful for some reason (which will be communicated as an error
     * code in the exception's message data).
     */
    public static PlaceConfig moveTo (BodyObject source, int placeId)
        throws ServiceFailedException
    {
        int bodoid = source.getOid();

        // make sure the place in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(placeId);
        if (pmgr == null) {
            Log.info("Requested to move to non-existent place " +
                     "[source=" + source + ", place=" + placeId + "].");
            throw new ServiceFailedException(NO_SUCH_PLACE);
        }

        // if they're already in the location they're asking to move to,
        // just give them the config because we don't need to update
        // anything in distributed object world
        if (source.location == placeId) {
            Log.info("Going along with client request to move to where " +
                     "they already are [source=" + source.who() +
                     ", placeId=" + placeId + "].");
            return pmgr.getConfig();
        }

        try {
            // acquire a lock on the body object to ensure that rapid fire
            // moveTo requests don't break things
            if (!source.acquireLock("moveToLock")) {
                // if we're still locked, a previous moveTo request hasn't
                // been fully processed
                throw new ServiceFailedException(MOVE_IN_PROGRESS);
            }

            PlaceObject place = pmgr.getPlaceObject();
            try {
                source.startTransaction();

                // remove them from any previous location
                leaveOccupiedPlace(source);

                // set the body's new location
                source.setLocation(place.getOid());

            } finally {
                source.commitTransaction();
            }

            try {
                place.startTransaction();

                // generate a new occupant info record and add it to the
                // target location
                OccupantInfo info = pmgr.buildOccupantInfo(source);
                if (info != null) {
                    place.addToOccupantInfo(info);
                }

                // add the body oid to the place object's occupant list
                place.addToOccupants(bodoid);

            } finally {
                place.commitTransaction();
            }

        } finally {
            // and finally queue up a lock release event to release the
            // lock once all these events are processed
            source.releaseLock("moveToLock");
        }

        return pmgr.getConfig();
    }

    /**
     * Removes the specified body from the place object they currently
     * occupy. Does nothing if the body is not currently in a place.
     */
    public static void leaveOccupiedPlace (BodyObject source)
    {
        int oldloc = source.location;
        int bodoid = source.getOid();

        // nothing to do if they weren't previously in some location
        if (oldloc == -1) {
            return;
        }

        // remove them from the occupant list
        try {
            PlaceObject pold = (PlaceObject)_omgr.getObject(oldloc);
            if (pold != null) {
                Object key = new Integer(bodoid);
                try {
                    pold.startTransaction();
                    // remove their occupant info (which is keyed on oid)
                    pold.removeFromOccupantInfo(key);
                    // and remove them from the occupant list
                    pold.removeFromOccupants(bodoid);

                } finally {
                    pold.commitTransaction();
                }

            } else {
                Log.info("Body's prior location no longer around? " +
                         "[boid=" + bodoid + ", poid=" + oldloc + "].");
            }

        } catch (ClassCastException cce) {
            Log.warning("Body claims to occupy non-PlaceObject!? " +
                        "[boid=" + bodoid + ", poid=" + oldloc +
                        ", error=" + cce + "].");
        }

        // clear out their location oid
        source.setLocation(-1);
    }

    /**
     * Forcibly moves the specified body object to the new place. This is
     * accomplished by first removing the client from their old location
     * and then sending the client a notification, instructing it to move
     * to the new location (which it does using the normal moveTo
     * service). This has the benefit that the client is removed from
     * their old place regardless of whether or not they are cooperating.
     * If they choose to ignore the forced move request, they will remain
     * in limbo, unable to do much of anything.
     */
    public void moveBody (BodyObject source, int placeId)
    {
        // first remove them from their old place
        leaveOccupiedPlace(source);

        // then send a move notification
        _invmgr.sendNotification(
            source.getOid(), MODULE_NAME, MOVE_NOTIFICATION,
            new Object[] { new Integer(placeId) });
    }

    /** The invocation manager with which we interoperate. */
    protected static InvocationManager _invmgr;

    /** The distributed object manager with which we interoperate. */
    protected static RootDObjectManager _omgr;

    /** The place registry with which we interoperate. */
    protected static PlaceRegistry _plreg;
}
