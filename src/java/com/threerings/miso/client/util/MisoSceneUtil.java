//
// $Id: MisoSceneUtil.java,v 1.2 2001/09/28 01:31:32 mdb Exp $

package com.threerings.miso.scene.util;

import java.util.List;

import com.threerings.miso.scene.*;

/**
 * Miso scene related utility functions and information.
 */
public class MisoSceneUtil
{
    /** String translations of each tile layer name. */
    public static final String[] XLATE_LAYERS = { "Base", "Fringe", "Object" };

    /**
     * Return the location object at the given full coordinates, or null
     * if no location is currently present at that location.
     *
     * @param scene the scene whose locations should be searched.
     * @param x the full x-position coordinate.
     * @param y the full y-position coordinate.
     *
     * @return the location object.
     */
    public static Location getLocation (MisoScene scene, int x, int y)
    {
        List locs = scene.getLocations();
	int size = locs.size();
	for (int ii = 0; ii < size; ii++) {
	    Location loc = (Location)locs.get(ii);
	    if (loc.x == x && loc.y == y) {
                return loc;
            }
	}
	return null;
    }

    /**
     * Return the portal with the given name, or null if the portal isn't
     * found in the portal list.
     *
     * @param scene the scene whose portals should be searched.
     * @param name the portal name.
     *
     * @return the portal object.
     */
    public static Portal getPortal (MisoScene scene, String name)
    {
        List portals = scene.getPortals();
	int size = portals.size();
	for (int ii = 0; ii < size; ii++) {
	    Portal portal = (Portal)portals.get(ii);
	    if (portal.name.equals(name)) {
                return portal;
            }
	}
	return null;
    }

    /**
     * Return the cluster index number the given location is in, or -1
     * if the location is not in any cluster.
     *
     * @param scene the containing scene.
     * @param loc the location.
     *
     * @return the cluster index or -1 if the location is not in any
     * cluster.
     */
    public static int getClusterIndex (MisoScene scene, Location loc)
    {
	return ClusterUtil.getClusterIndex(scene.getClusters(), loc);
    }
}
