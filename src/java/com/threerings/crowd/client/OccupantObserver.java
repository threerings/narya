//
// $Id: OccupantObserver.java,v 1.4 2002/10/27 01:25:08 mdb Exp $

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
     * Called when an occupant is updated.
     *
     * @param oldinfo the occupant info prior to the update.
     * @param newinfo the newly update info record.
     */
    public void occupantUpdated (OccupantInfo oldinfo, OccupantInfo newinfo);
}
