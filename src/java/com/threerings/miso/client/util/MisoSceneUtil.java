//
// $Id: MisoSceneUtil.java,v 1.3 2001/10/11 00:41:27 shaper Exp $

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
     * Returns the layer index number for the named layer.  Layer
     * names are looked up via <code>XLATE_LAYERS</code> and are
     * case-insensitive.
     *
     * @param name the layer name.
     */
    public static int getLayerIndex (String name)
    {
	if (name == null) {
	    return DEF_LAYER;
	}

	name = name.toLowerCase();

	for (int ii = 0; ii < MisoScene.NUM_LAYERS; ii++) {
	    String b = MisoSceneUtil.XLATE_LAYERS[ii].toLowerCase();
	    if (name.equals(b)) {
		return ii;
	    }
	}

	return DEF_LAYER;
    }

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

    /** The default layer index for an unknown named layer. */
    protected static final int DEF_LAYER = MisoScene.LAYER_BASE;
}
