//
// $Id: DisplaySpotScene.java,v 1.1 2001/11/13 02:25:35 mdb Exp $

package com.threerings.whirled.spot.client;

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
     * Returns an array of the locations in this scene (including portals
     * which will be instances of {@link Portal}).
     */
    public Location[] getLocations ();

    /**
     * Returns an array of the portals in this scene.
     */
    public Portal[] getPortals ();
}
