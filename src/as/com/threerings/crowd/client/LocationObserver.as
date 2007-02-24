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

package com.threerings.crowd.client {

import com.threerings.crowd.data.PlaceObject;

/**
 * The location observer interface makes it possible for entities to be
 * notified when the client moves to a new location. It also provides a
 * means for an entity to participate in the ratification process of a new
 * location. Observers may opt to reject a request to change to a new
 * location, probably because something is going on in the previous
 * location that should not be abandoned.
 *
 * <p> Note that these location callbacks occur on the main thread and
 * should execute quickly and not block under any circumstance.
 */
public interface LocationObserver
{
    /**
     * Called when someone has requested that we switch to a new location.
     * An observer may choose to veto the location change request for some
     * reason or other.
     *
     * @return true if it's OK for the location to change, false if the
     * change request should be aborted.
     */
    function locationMayChange (placeId :int) :Boolean;

    /**
     * Called when we have switched to a new location.
     *
     * @param place the place object that represents the new location or
     * null if we have switched to no location.
     */
    function locationDidChange (place :PlaceObject) :void;

    /**
     * This is called on all location observers when a location change
     * request is rejected by the server or fails for some other reason.
     *
     * @param placeId the place id to which we attempted to relocate, but
     * failed.
     * @param reason the reason code that explains why the location change
     * request was rejected or otherwise failed.
     */
    function locationChangeFailed (placeId :int, reason :String) :void;
}
}
