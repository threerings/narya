//
// $Id: OccupantObserver.java,v 1.3 2002/04/15 14:38:45 shaper Exp $

package com.threerings.crowd.client;

import com.threerings.crowd.data.OccupantInfo;

/**
 * An entity that is interested in hearing about bodies that enter and
 * leave a location (as well as disconnect and reconnect) can implement
 * this interface and register itself with the {@link OccupantDirector}.
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
