//
// $Id: DisplaySpotScene.java,v 1.4 2001/12/16 21:22:30 mdb Exp $

package com.threerings.whirled.spot.client;

import java.util.List;

import com.threerings.whirled.client.DisplayScene;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;

/**
 * Makes available the spot scene information that the client needs to
 * display a spot scene, to make requests to the server to move from
 * location to location and to neighboring scenes and to display the same.
 */
public interface DisplaySpotScene extends DisplayScene
{
    /**
     * Returns the location id of the default entrance to this scene.
     */
    public int getDefaultEntranceId ();

    /**
     * Returns a list of the locations in this scene (including portals
     * which will be instances of {@link Portal}).
     */
    public List getLocations ();

    /**
     * Convenience function for obtaining a location's index in the
     * location list given its id.
     *
     * @return the location's index or -1 if a location with the specified
     * id is not in this scene's location list.
     */
    public int getLocationIndex (int locationId);

    /**
     * Convenience funtion for looking up a location by id.
     */
    public Location getLocation (int locationId);

    /**
     * Returns a list of the portals in this scene.
     */
    public List getPortals ();
}
