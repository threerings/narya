//
// $Id: SpotOccupantInfo.java,v 1.3 2002/06/20 22:13:20 mdb Exp $

package com.threerings.whirled.spot.data;

import com.samskivert.util.StringUtil.Formatter;

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

    /** Used to log just the user oids and location ids of users in the
     * <code>occupantInfo</code> set when debugging. */
    public static final Formatter OIDS_AND_LOCS = new Formatter() {
        public String toString (Object object) {
            SpotOccupantInfo soi = (SpotOccupantInfo)object;
            return "[" + soi.getBodyOid() + ", " + soi.locationId + "]";
        }
    };
}
