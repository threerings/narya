//
// $Id: SpotScene.java,v 1.1 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.data;

import java.util.Iterator;

/**
 * Makes available the spot scene information that the server needs to do
 * its business.
 */
public interface SpotScene
{
    /**
     * Returns a {@link Portal} object for the portal with the specified
     * id or null if no portal exists with that id.
     */
    public Portal getPortal (int portalId);

    /**
     * Returns the number of portals in this scene.
     */
    public int getPortalCount ();

    /**
     * Returns an iterator over the portals in this scene.
     */
    public Iterator getPortals ();

    /**
     * Returns the portal that represents the default entrance to this
     * scene. If a body enters the scene at logon time rather than
     * entering from some other scene, this is the portal at which they
     * would appear.
     */
    public Portal getDefaultEntrance ();

    /**
     * Adds a portal to this scene, immediately making the requisite
     * modifications to the underlying scene model. A portal id will be
     * assigned to the portal by this method.
     */
    public void addPortal (Portal portal);

    /**
     * Removes the specified portal from the scene.
     */
    public void removePortal (Portal portal);

    /**
     * Sets the default entrance in this scene, immediately making the
     * requisite modifications to the underlying scene model.
     */
    public void setDefaultEntrance (Portal portal);
}
