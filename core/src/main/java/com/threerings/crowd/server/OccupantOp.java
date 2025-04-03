//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import com.threerings.crowd.data.OccupantInfo;

/**
 * An operation to be applied to all occupants in a location that may
 * contain occupants, e.g., a {@link PlaceManager}.
 */
public interface OccupantOp
{
    /**
     * Called with the occupant info for each occupant in the location.
     */
    void apply (OccupantInfo info);
}
