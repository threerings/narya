//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;

import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.client.LocationService;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.LocationCodes;
import com.threerings.crowd.data.Place;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import static com.threerings.crowd.Log.log;

/**
 * Handles location-related services.
 */
@Singleton
public class LocationManager
    implements LocationProvider, LocationCodes
{
    @Inject public LocationManager (InvocationManager invmgr)
    {
        invmgr.registerDispatcher(new LocationDispatcher(this), CrowdCodes.CROWD_GROUP);
    }

    // from interface LocationProvider
    public void moveTo (ClientObject caller, int placeOid, LocationService.MoveListener listener)
        throws InvocationException
    {
        // do the move and send the response
        listener.moveSucceeded(moveTo((BodyObject)caller, placeOid));
    }

    // from interface LocationProvider
    public void leavePlace (ClientObject caller)
    {
        leaveOccupiedPlace((BodyObject)caller);
    }

    /**
     * Moves the specified body from whatever location they currently occupy to the location
     * identified by the supplied place oid.
     *
     * @return the config object for the new location.
     *
     * @exception ServiceFaildException thrown if the move was not successful for some reason
     * (which will be communicated as an error code in the exception's message data).
     */
    public PlaceConfig moveTo (BodyObject source, int placeOid)
        throws InvocationException
    {
        int bodoid = source.getOid();

        // make sure the place in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(placeOid);
        if (pmgr == null) {
            log.info("Requested to move to non-existent place [who=" + source.who() +
                     ", placeOid=" + placeOid + "].");
            throw new InvocationException(NO_SUCH_PLACE);
        }

        // if they're already in the location they're asking to move to, just give them the config
        // because we don't need to update anything in distributed object world
        Place place = pmgr.getLocation();
        if (place.equals(source.location)) {
            log.debug("Going along with client request to move to where they already are " +
                      "[source=" + source.who() + ", place=" + place + "].");
            return pmgr.getConfig();
        }

        // make sure they have access to the specified place
        String errmsg;
        if ((errmsg = pmgr.ratifyBodyEntry(source)) != null) {
            throw new InvocationException(errmsg);
        }

        // acquire a lock on the body object to avoid breakage by rapid fire moveTo requests
        if (!source.acquireLock("moveToLock")) {
            // if we're still locked, a previous moveTo request hasn't been fully processed
            throw new InvocationException(MOVE_IN_PROGRESS);
        }

        // configure the client accordingly if the place uses a custom class loader
        PresentsClient client = _clmgr.getClient(source.username);
        if (client != null) {
            client.setClassLoader(pmgr.getClass().getClassLoader());
        }

        try {
            PlaceObject plobj = pmgr.getPlaceObject();

            // the doubly nested try catch is to prevent failure if one or the other of the
            // transactions fails to start
            plobj.startTransaction();
            try {
                source.startTransaction();
                try {
                    // remove them from any previous location
                    leaveOccupiedPlace(source);

                    // generate a new occinfo record (which will add it to the target location)
                    pmgr.buildOccupantInfo(source);

                    // set the body's new location
                    source.willEnterPlace(place, plobj);

                    // add the body oid to the place object's occupant list
                    plobj.addToOccupants(bodoid);

                } finally {
                    source.commitTransaction();
                }
            } finally {
                plobj.commitTransaction();
            }

        } finally {
            // and finally queue up an event to release the lock once these events are processed
            source.releaseLock("moveToLock");
        }

        return pmgr.getConfig();
    }

    /**
     * Removes the specified body from the place object they currently occupy. Does nothing if the
     * body is not currently in a place.
     */
    public void leaveOccupiedPlace (BodyObject source)
    {
        Place oldloc = source.location;
        int bodoid = source.getOid();

        // nothing to do if they weren't previously in some location
        if (oldloc == null) {
            return;
        }

        // remove them from the occupant list
        PlaceObject plobj = null;
        try {
            plobj = (PlaceObject)_omgr.getObject(oldloc.placeOid);
            if (plobj != null) {
                Integer key = Integer.valueOf(bodoid);
                plobj.startTransaction();
                try {
                    // remove their occupant info (which is keyed on oid)
                    plobj.removeFromOccupantInfo(key);
                    // and remove them from the occupant list
                    plobj.removeFromOccupants(bodoid);

                } finally {
                    plobj.commitTransaction();
                }

            } else {
                log.info("Body's prior location no longer around? [boid=" + bodoid +
                         ", place=" + oldloc + "].");
            }

        } catch (ClassCastException cce) {
            log.warning("Body claims to occupy non-PlaceObject!? [boid=" + bodoid +
                        ", place=" + oldloc + ", error=" + cce + "].");
        }

        // clear out their location
        source.didLeavePlace(plobj);
    }

    /**
     * Forcibly moves the specified body object to the new place. This is accomplished by first
     * removing the client from their old location and then sending the client a notification,
     * instructing it to move to the new location (which it does using the normal moveTo service).
     * This has the benefit that the client is removed from their old place regardless of whether
     * or not they are cooperating.  If they choose to ignore the forced move request, they will
     * remain in limbo, unable to do much of anything.
     */
    public void moveBody (BodyObject source, Place place)
    {
        // first remove them from their old place
        leaveOccupiedPlace(source);

        // then send a forced move notification
        LocationSender.forcedMove(source, place.placeOid);
    }

    @Inject protected RootDObjectManager _omgr;
    @Inject protected PlaceRegistry _plreg;
    @Inject protected ClientManager _clmgr;
}
