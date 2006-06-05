//
// $Id: OccupantDirector.java 3406 2005-03-15 02:12:03Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.client {

import com.threerings.util.Iterator;
import com.threerings.util.ObserverList;
import com.threerings.util.Name;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

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
    public function OccupantDirector (ctx :CrowdContext)
    {
        super(ctx);

        // register ourselves as a location observer
        ctx.getLocationDirector().addLocationObserver(this);
    }

    /**
     * Adds the specified occupant observer to the list.
     */
    public function addOccupantObserver (obs :OccupantObserver) :void
    {
        _observers.add(obs);
    }

    /**
     * Removes the specified occupant observer from the list.
     */
    public function removeOccupantObserver (obs :OccupantObserver) :void
    {
        _observers.remove(obs);
    }

    /**
     * Returns the occupant info for the user in question if it exists in
     * the currently occupied place. Returns null if no occupant info
     * exists for the specified body.
     */
    public function getOccupantInfo (bodyOid :int) :OccupantInfo
    {
        // make sure we're somewhere
        return (_place == null) ? null :
            (_place.occupantInfo.get(bodyOid) as OccupantInfo);
    }

    /**
     * Returns the occupant info for the user in question if it exists in
     * the currently occupied place. Returns null if no occupant info
     * exists with the specified username.
     */
    public function getOccupantInfoByName (username :Name) :OccupantInfo
    {
        return (_place == null) ? null : _place.getOccupantInfo(username);
    }

    // documentation inherited
    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);
        // clear things out
        if (_place != null) {
            _place.removeListener(this);
            _place = null;
        }
    }

    // documentation inherited from interface LocationObserver
    public function locationMayChange (placeId :int) :Boolean
    {
        // we've got no opinion
        return true;
    }

    // documentation inherited from interface LocationObserver
    public function locationDidChange (place :PlaceObject) :void
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

    // documentation inherited from interface LocationObserver
    public function locationChangeFailed (placeId :int, reason :String) :void
    {
        // nothing to do here either
    }

    // documentation inherited from interface SetListener
    public function entryAdded (event :EntryAddedEvent) :void
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (event.getName() != PlaceObject.OCCUPANT_INFO) {
            return;
        }

        // now let the occupant observers know what's up
        var info :OccupantInfo = (event.getEntry() as OccupantInfo);
        _observers.apply(function (obj :Object) :Boolean {
            (obj as OccupantObserver).occupantEntered(info);
            return true;
        });
    }

    // documentation inherited from interface SetListener
    public function entryUpdated (event :EntryUpdatedEvent) :void
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (event.getName() != PlaceObject.OCCUPANT_INFO) {
            return;
        }

        // now let the occupant observers know what's up
        var info :OccupantInfo = (event.getEntry() as OccupantInfo);
        var oinfo :OccupantInfo = (event.getOldEntry() as OccupantInfo);
        _observers.apply(function (obj :Object) :Boolean {
            (obj as OccupantObserver).occupantUpdated(oinfo, info);
            return true;
        });
    }

    // documentation inherited from interface SetListener
    public function entryRemoved (event :EntryRemovedEvent) :void
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (event.getName() != PlaceObject.OCCUPANT_INFO) {
            return;
        }

        // let the occupant observers know what's up
        var oinfo :OccupantInfo = (event.getOldEntry() as OccupantInfo);
        _observers.apply(function (obj :Object) :Boolean {
            (obj as OccupantObserver).occupantLeft(oinfo);
            return true;
        });
    }

    /** The occupant observers to keep abreast of occupant antics. */
    protected var _observers :ObserverList =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The user's current location. */
    protected var _place :PlaceObject;
}
}
