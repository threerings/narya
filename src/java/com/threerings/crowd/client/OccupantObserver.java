//
// $Id: OccupantObserver.java,v 1.1 2001/08/20 20:54:56 mdb Exp $

package com.threerings.cocktail.party.client;

import com.threerings.cocktail.party.data.OccupantInfo;

/**
 * An entity that is interested in hearing about bodies that enter and
 * leave a location (as well as disconnect and reconnect) can implement
 * this interface and register itself with the {@link OccupantManager}.
 */
public interface OccupantObserver
{
    /**
     * Called when a body enters the place.
     */
    public void occupantEntered (OccupantInfo info);

    /**
     * Called when a body leaves the place.
     */
    public void occupantLeft (OccupantInfo info);

    /**
     * Called an occupant is updated.
     */
    public void occupantUpdated (OccupantInfo info);
}
