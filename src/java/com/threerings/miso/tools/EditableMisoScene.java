//
// $Id: EditableMisoScene.java,v 1.3 2001/10/13 01:08:59 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.tile.MisoTile;

/**
 * The editable Miso scene interface provides the means for modifying a
 * Miso scene which is needed by things like the scene editor. This is
 * separated from the read-only interface to avoid implying to users of
 * the Miso scene interface that editing scenes is a safe thing to do. In
 * fact it should only be done under special circumstances.
 */
public interface EditableMisoScene
    extends MisoScene
{
    /**
     * Updates the scene's unique identifier.
     */
    public void setId (int sceneId);

    /**
     * Updates the scene's name.
     */
    public void setName (String name);

    /**
     * Updates the scene's default tile.
     */
    public void setDefaultTile (MisoTile tile);

    /**
     * Set the default entrance portal for this scene.
     *
     * @param entrance the entrance portal.
     */
    public void setEntrance (Portal entrance);

    /**
     * Update the specified location in the scene.  If the cluster
     * index number is -1, the location will be removed from any
     * cluster it may reside in.
     *
     * @param loc the location.
     * @param clusteridx the cluster index number.
     */
    public void updateLocation (Location loc, int clusteridx);

    /**
     * Add the specified portal to the scene.  Adds the portal to the
     * location list as well if it's not already present and removes
     * it from any cluster it may reside in.
     *
     * @param portal the portal.
     */
    public void addPortal (Portal portal);

    /**
     * Remove the given location object from the location list, and
     * from any containing cluster.  If the location is a portal, it
     * is removed from the portal list as well.
     *
     * @param loc the location object.
     */
    public void removeLocation (Location loc);
}
