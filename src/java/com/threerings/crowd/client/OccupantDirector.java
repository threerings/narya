//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.client;

import java.util.Iterator;

import com.samskivert.util.ObserverList;
import com.threerings.util.Name;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * The occupant director listens for occupants of places to enter and
 * exit, and dispatches notices to interested parties about these events.
 *
 * <p> It will eventually provide a framework for keeping track of
 * occupant information in a network efficient manner. The idea being that
 * we want to store as little information about occupants as possible in
 * the place object (probably just body oid and username), but upon
 * entering a place, this will be all we know about the occupants. We then
 * dispatch a request to get information about all of the occupants in the
 * room (things like avatar information for a graphical display or perhaps
 * their ratings in the game that is associated with a place for a gaming
 * site) which we then pass on to the occupant observers when it becomes
 * available.
 *
 * <p> This information would be cached and we could return cached
 * information for occupants for which we have cached info. We will
 * probably want to still make a request for the occupant info so that we
 * can update non-static occupant data rather than permanently using
 * what's in the cache.
 */
public class OccupantDirector extends BasicDirector
    implements LocationObserver, SetListener
{
    /**
     * Constructs a new occupant director with the supplied context.
     */
    public OccupantDirector (CrowdContext ctx)
    {
        super(ctx);

        // register ourselves as a location observer
        ctx.getLocationDirector().addLocationObserver(this);
    }

    /**
     * Adds the specified occupant observer to the list.
     */
    public void addOccupantObserver (OccupantObserver obs)
    {
        _observers.add(obs);
    }

    /**
     * Removes the specified occupant observer from the list.
     */
    public void removeOccupantObserver (OccupantObserver obs)
    {
        _observers.remove(obs);
    }

    /**
     * Returns the occupant info for the user in question if it exists in
     * the currently occupied place. Returns null if no occupant info
     * exists for the specified body.
     */
    public OccupantInfo getOccupantInfo (int bodyOid)
    {
        // make sure we're somewhere
        return (_place == null) ? null :
            (OccupantInfo)_place.occupantInfo.get(Integer.valueOf(bodyOid));
    }

    /**
     * Returns the occupant info for the user in question if it exists in
     * the currently occupied place. Returns null if no occupant info
     * exists with the specified username.
     */
    public OccupantInfo getOccupantInfo (Name username)
    {
        return (_place == null) ? null : _place.getOccupantInfo(username);
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        // clear things out
        if (_place != null) {
            _place.removeListener(this);
            _place = null;
        }
    }

    // inherit documentation
    public boolean locationMayChange (int placeId)
    {
        // we've got no opinion
        return true;
    }

    // inherit documentation
    public void locationDidChange (PlaceObject place)
    {
        // unlisten to the old place object if there was one
        if (_place != null) {
            _place.removeListener(this);
        }

        // listen to the new one
        _place = place;
        if (_place != null) {
            _place.addListener(this);
        }
    }

    // inherit documentation
    public void locationChangeFailed (int placeId, String reason)
    {
        // nothing to do here either
    }

    /**
     * Deals with all of the processing when an occupant shows up.
     */
    public void entryAdded (EntryAddedEvent event)
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (!event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            return;
        }

        // now let the occupant observers know what's up
        final OccupantInfo info = (OccupantInfo)event.getEntry();
        _observers.apply(new ObserverList.ObserverOp<OccupantObserver>() {
            public boolean apply (OccupantObserver observer) {
                observer.occupantEntered(info);
                return true;
            }
        });
    }

    /**
     * Deals with all of the processing when an occupant is updated.
     */
    public void entryUpdated (EntryUpdatedEvent event)
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (!event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            return;
        }

        // now let the occupant observers know what's up
        final OccupantInfo info = (OccupantInfo) event.getEntry();
        final OccupantInfo oinfo = (OccupantInfo) event.getOldEntry();
        _observers.apply(new ObserverList.ObserverOp<OccupantObserver>() {
            public boolean apply (OccupantObserver observer) {
                observer.occupantUpdated(oinfo, info);
                return true;
            }
        });
    }

    /**
     * Deals with all of the processing when an occupant leaves.
     */
    public void entryRemoved (EntryRemovedEvent event)
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (!event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            return;
        }

        // let the occupant observers know what's up
        final OccupantInfo oinfo = (OccupantInfo) event.getOldEntry();
        _observers.apply(new ObserverList.ObserverOp<OccupantObserver>() {
            public boolean apply (OccupantObserver observer) {
                observer.occupantLeft(oinfo);
                return true;
            }
        });
    }

    /** The occupant observers to keep abreast of occupant antics. */
    protected ObserverList<OccupantObserver> _observers =
        new ObserverList<OccupantObserver>(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The user's current location. */
    protected PlaceObject _place;
}
