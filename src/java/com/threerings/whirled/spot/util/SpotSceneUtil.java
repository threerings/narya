//
// $Id: SpotSceneUtil.java,v 1.1 2002/06/20 22:14:58 mdb Exp $

package com.threerings.whirled.spot.util;

import java.util.Iterator;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.StringUtil;

import com.threerings.util.RandomUtil;

import com.threerings.presents.dobj.DSet;
import com.threerings.whirled.spot.data.SpotOccupantInfo;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * Spot scene utility functions shared by client and server.
 */
public class SpotSceneUtil
{
    /**
     * Returns the locationId of an unoccupied location in the supplied
     * scene (portals are not included when selecting). If no locations
     * are unoccupied, this method returns -1.
     *
     * @param preferClusters if true, cluster locations (if any are empty)
     * will be preferred.
     */
    public static int getUnoccupiedLocation (
        SpotSceneModel model, DSet occupantInfo, boolean preferClusters)
    {
        ArrayIntSet locs = getStartSet(model, preferClusters);

        // remove any occupied locations
        Iterator iter = occupantInfo.entries();
        while (iter.hasNext()) {
            SpotOccupantInfo yoi = (SpotOccupantInfo)iter.next();
            locs.remove(yoi.locationId);
        }

        // if there are locations left, pick one at random
        int size = locs.size();
        if (size > 0) {
           return locs.get(RandomUtil.getInt(size));

        } else if (preferClusters) {
            // retry without the preference
            return getUnoccupiedLocation(model, occupantInfo, false);

        } else {
            return -1; // we didn't find anything.
        }
    }

    /**
     * Returns the locationId of an unoccupied location in the supplied
     * scene (portals are not included when selecting). If no locations
     * are unoccupied, this method returns -1.
     *
     * @param preferClusters if true, cluster locations (if any are empty)
     * will be preferred.
     */
    public static int getUnoccupiedLocation (
        SpotSceneModel model, int[] occupiedLocs, boolean preferClusters)
    {
        ArrayIntSet locs = getStartSet(model, preferClusters);

        // remove any occupied locations
        for (int ii = 0; ii < model.locationIds.length; ii++) {
            if (occupiedLocs[ii] != 0) {
                locs.remove(model.locationIds[ii]);
            }
        }

        // if there are locations left, pick one at random
        int size = locs.size();
        if (size > 0) {
           return locs.get(RandomUtil.getInt(size));

        } else if (preferClusters) {
            // retry without the preference
            return getUnoccupiedLocation(model, occupiedLocs, false);

        } else {
            return -1; // we didn't find anything.
        }
    }

    /** Used by {@link #getUnoccupiedLocation}. */
    protected static ArrayIntSet getStartSet (
        SpotSceneModel model, boolean preferClusters)
    {
        ArrayIntSet locs = new ArrayIntSet();

        if (preferClusters) {
            // only add locations with a valid cluster Id
            for (int ii=0; ii < model.locationIds.length; ii++) {
                if (model.locationClusters[ii] != 0) {
                    locs.add(model.locationIds[ii]);
                }
            }

        } else {
            // start with all the locations in the scene
            locs.add(model.locationIds);

            // remove the portals
            locs.remove(model.portalIds);
        }

        return locs;
    }
}
