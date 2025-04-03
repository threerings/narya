//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import com.threerings.crowd.data.OccupantInfo;

/**
 * An entity that is interested in hearing about bodies that enter and leave a location (as well
 * as disconnect and reconnect) can implement this interface and register itself with the
 * {@link OccupantDirector}.
 */
public interface OccupantObserver
{
    /**
     * Called when a body enters the place.
     */
    void occupantEntered (OccupantInfo info);

    /**
     * Called when a body leaves the place.
     */
    void occupantLeft (OccupantInfo info);

    /**
     * Called when an occupant is updated.
     *
     * @param oldinfo the occupant info prior to the update.
     * @param newinfo the newly update info record.
     */
    void occupantUpdated (OccupantInfo oldinfo, OccupantInfo newinfo);
}
