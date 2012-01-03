//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
import com.threerings.presents.server.PresentsSession;

import com.threerings.crowd.client.LocationService;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.LocationCodes;
import com.threerings.crowd.data.LocationMarshaller;
import com.threerings.crowd.data.Place;
import com.threerings.crowd.data.PlaceConfig;

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
        invmgr.registerProvider(this, LocationMarshaller.class, CrowdCodes.CROWD_GROUP);
    }

    // from interface LocationProvider
    public void moveTo (ClientObject caller, int placeOid, LocationService.MoveListener listener)
        throws InvocationException
    {
        // do the move and send the response
        BodyObject body = _locator.forClient(caller);
        listener.moveSucceeded(moveTo(body, placeOid));
    }

    // from interface LocationProvider
    public void leavePlace (ClientObject caller)
    {
        BodyObject body = _locator.forClient(caller);
        leaveOccupiedPlace(body);
    }

    /**
     * Moves the specified body from whatever location they currently occupy to the location
     * identified by the supplied place oid.
     *
     * @return the config object for the new location.
     *
     * @exception InvocationException thrown if the move was not successful for some reason
     * (which will be communicated as an error code in the exception's message data).
     */
    public PlaceConfig moveTo (BodyObject source, int placeOid)
        throws InvocationException
    {
        // make sure the place in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(placeOid);
        if (pmgr == null) {
            log.info("Requested to move to non-existent place", "who", source.who(),
                     "placeOid", placeOid);
            throw new InvocationException(NO_SUCH_PLACE);
        }

        // if they're already in the location they're asking to move to, just give them the config
        // because we don't need to update anything in distributed object world
        Place place = pmgr.getLocation();
        if (place.equals(source.location)) {
            log.debug("Going along with client request to move to where they already are",
                      "source", source.who(), "place", place);
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
        PresentsSession client = _clmgr.getClient(source.username);
        if (client != null) {
            client.setClassLoader(pmgr.getClass().getClassLoader());
        }

        try {
            source.startTransaction();
            try {
                // remove them from any previous location
                leaveOccupiedPlace(source);

                // let the place manager know that we're coming in
                pmgr.bodyWillEnter(source);

                // let the body object know that it's going in
                source.willEnterPlace(place, pmgr.getPlaceObject());

            } finally {
                source.commitTransaction();
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
        if (oldloc == null) {
            return; // nothing to do if they weren't previously in some location
        }

        PlaceManager pmgr = _plreg.getPlaceManager(oldloc.placeOid);
        if (pmgr == null) {
            log.warning("Body requested to leave no longer existent place?",
                        "boid", source.getOid(), "place", oldloc);
            return;
        }

        // tell the place manager that they're on the way out
        pmgr.bodyWillLeave(source);

        // clear out their location
        source.didLeavePlace(pmgr.getPlaceObject());
    }

    /**
     * Forcibly moves the specified body object to the new place. This is accomplished by first
     * removing the body from their old location and then sending the client a notification,
     * instructing it to move to the new location (which it does using the normal moveTo service).
     * This has the benefit that the client is removed from their old place regardless of whether
     * or not they are cooperating.  If they choose to ignore the forced move request, they will
     * remain in limbo, unable to do much of anything.
     */
    public void moveBody (BodyObject source, Place place)
    {
        // first remove them from their old place
        leaveOccupiedPlace(source);

        // then send a forced move notification to the body's client
        LocationSender.forcedMove(source.getClientObject(), place.placeOid);
    }

    @Inject protected RootDObjectManager _omgr;
    @Inject protected BodyLocator _locator;
    @Inject protected ClientManager _clmgr;
    @Inject protected PlaceRegistry _plreg;
}
