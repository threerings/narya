//
// $Id: LocationProvider.java,v 1.16 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.dobj.RootDObjectManager;

import com.threerings.presents.data.ClientObject;

import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.Log;
import com.threerings.crowd.client.LocationService;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.LocationCodes;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

/**
 * This class provides the server end of the location services.
 */
public class LocationProvider
    implements LocationCodes, InvocationProvider
{
    /**
     * Creates a location provider and prepares it for operation.
     */
    public LocationProvider (InvocationManager invmgr, RootDObjectManager omgr,
                             PlaceRegistry plreg)
    {
        // we'll need these later
        _invmgr = invmgr;
        _omgr = omgr;
        _plreg = plreg;
    }

    /**
     * Requests that this client's body be moved to the specified
     * location.
     *
     * @param caller the client object of the client that invoked this
     * remotely callable method.
     * @param placeId the object id of the place object to which the body
     * should be moved.
     * @param listener the listener that will be informed of success or
     * failure.
     */
    public void moveTo (ClientObject caller, int placeId,
                        LocationService.MoveListener listener)
        throws InvocationException
    {
        // do the move and send the response
        listener.moveSucceeded(moveTo((BodyObject)caller, placeId));
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
    public PlaceConfig moveTo (BodyObject source, int placeId)
        throws InvocationException
    {
        int bodoid = source.getOid();

        // make sure the place in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(placeId);
        if (pmgr == null) {
            Log.info("Requested to move to non-existent place " +
                     "[source=" + source + ", place=" + placeId + "].");
            throw new InvocationException(NO_SUCH_PLACE);
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
                throw new InvocationException(MOVE_IN_PROGRESS);
            }

            PlaceObject place = pmgr.getPlaceObject();
            try {
                place.startTransaction();
                source.startTransaction();

                // remove them from any previous location
                leaveOccupiedPlace(source);

                // generate a new occupant info record and add it to the
                // target location
                OccupantInfo info = pmgr.buildOccupantInfo(source);
                if (info != null) {
                    place.addToOccupantInfo(info);
                }

                // set the body's new location
                source.setLocation(place.getOid());

                // add the body oid to the place object's occupant list
                place.addToOccupants(bodoid);

            } finally {
                place.commitTransaction();
                source.commitTransaction();
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
    public void leaveOccupiedPlace (BodyObject source)
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

        // then send a forced move notification
        LocationSender.forcedMove(source, placeId);
    }

    /** The invocation manager with which we interoperate. */
    protected InvocationManager _invmgr;

    /** The distributed object manager with which we interoperate. */
    protected RootDObjectManager _omgr;

    /** The place registry with which we interoperate. */
    protected PlaceRegistry _plreg;
}
