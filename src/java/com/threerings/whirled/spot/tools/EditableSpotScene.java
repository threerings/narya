//
// $Id: EditableSpotScene.java,v 1.5 2001/12/05 09:20:10 mdb Exp $

package com.threerings.whirled.tools.spot;

import com.threerings.whirled.tools.EditableScene;

import com.threerings.whirled.spot.client.DisplaySpotScene;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * The editable spot scene interface is used in the offline scene building
 * tools as well as by the tools that load those prototype scenes into the
 * runtime database. Accordingly, it provides a means for modifying scene
 * values and for obtaining access to the underlying scene models that
 * represent the underlying scene information.
 *
 * @see EditableScene
 */
public interface EditableSpotScene
    extends DisplaySpotScene, EditableScene
{
    /**
     * Sets the location id of the default entrance to this scene.
     */
    public void setDefaultEntranceId (int defaultEntranceId);

    /**
     * Returns the next valid location id for this scene. Newly created
     * portals or locations should be assigned a new location id via this
     * method.
     */
    public int getNextLocationId ();

    /**
     * Adds a location to this scene.
     */
    public void addLocation (Location location);

    /**
     * Removes the specified location from the scene.
     */
    public void removeLocation (Location location);

    /**
     * Adds a portal to this scene (it should be added appropriately to
     * both the location and portal arrays).
     */
    public void addPortal (EditablePortal portal);

    /**
     * Removes a portal from this scene (it should be removed accordingly
     * from both the location and portal arrays).
     */
    public void removePortal (EditablePortal portal);

    /** 
     * Implementations must provide a scene model that represents the
     * current state of this editable scene in response to a call to this
     * method. Whether they maintain an up to date scene model all along
     * or generate one at the time this method is called is up to the
     * implementation.
     */
    public EditableSpotSceneModel getSpotSceneModel ();
}
