//
// $Id: DisplaySpotScene.java,v 1.3 2001/12/05 08:45:05 mdb Exp $

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
     * Returns a list of the portals in this scene.
     */
    public List getPortals ();
}
