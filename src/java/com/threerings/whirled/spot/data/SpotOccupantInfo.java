//
// $Id: SpotOccupantInfo.java,v 1.2 2002/04/17 22:19:40 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.crowd.data.OccupantInfo;

/**
 * The spot services extend the basic occupant info with information on
 * which location within the scene a user occupies.
 */
public class SpotOccupantInfo extends OccupantInfo
{
    /** The id of the location occupied by this user or -1 if they occupy
     * no location. */
    public int locationId;
}
