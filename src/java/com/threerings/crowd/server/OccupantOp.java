//
// $Id: OccupantOp.java,v 1.1 2002/10/31 01:12:08 shaper Exp $

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
    public void apply (OccupantInfo info);
}
