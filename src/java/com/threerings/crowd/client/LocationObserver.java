//
// $Id: LocationObserver.java,v 1.4 2001/10/11 04:07:51 mdb Exp $

package com.threerings.crowd.client;

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
    public boolean locationMayChange (int placeId);

    /**
     * Called when we have switched to a new location.
     *
     * @param place the place object that represents the new location or
     * null if we have switched to no location.
     */
    public void locationDidChange (PlaceObject place);

    /**
     * This is called on all location observers when a location change
     * request is rejected by the server or fails for some other reason.
     *
     * @param placeId the place id to which we attempted to relocate, but
     * failed.
     * @param reason the reason code that explains why the location change
     * request was rejected or otherwise failed.
     */
    public void locationChangeFailed (int placeId, String reason);
}
