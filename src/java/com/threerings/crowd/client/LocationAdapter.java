//
// $Id: LocationAdapter.java,v 1.2 2001/10/11 04:07:51 mdb Exp $

package com.threerings.crowd.client;

import com.threerings.crowd.data.PlaceObject;

/**
 * The location adapter makes life easier for a class that really only
 * cares about one or two of the location observer callbacks and doesn't
 * want to provide empty implementations of the others. One can either
 * extend location adapter, or create an anonymous instance that overrides
 * the desired callback(s). Note that the location adapter defaults to
 * ratifying any location change.
 *
 * @see LocationObserver
 */
public class LocationAdapter implements LocationObserver
{
    // documentation inherited
    public boolean locationMayChange (int placeId)
    {
        return true;
    }

    // documentation inherited
    public void locationDidChange (PlaceObject place)
    {
    }

    // documentation inherited
    public void locationChangeFailed (int placeId, String reason)
    {
    }
}
