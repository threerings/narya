//
// $Id: DisplayScene.java,v 1.1 2001/11/12 20:56:55 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.crowd.data.PlaceConfig;

/**
 * This interface makes available the scene information that is needed by
 * a client to display a scene. At this basic level, not much information
 * is available, but extensions to this interface begin to create a more
 * comprehensive picture of a scene in a system built from the Whirled
 * services.
 *
 * <p> Additionally, at this basic level, the <code>DisplayScene</code>
 * does not differ greatly (or at all) from the {@link
 * com.threerings.whirled.server.RuntimeScene} interface, but the
 * distinction provides a mechanism for handling the more substantial
 * differences that appear in more sophisticated extensions to the base
 * scene services.
 */
public interface DisplayScene
{
    /**
     * Returns the unique identifier for this scene.
     */
    public int getId ();

    /**
     * Returns the version number of this scene.
     */
    public int getVersion ();

    /**
     * Returns the list of scene ids of this scene's neighbors.
     */
    public int[] getNeighborIds ();

    /**
     * Returns the place config that can be used to determine which place
     * controller instance should be used to display this scene as well as
     * to obtain runtime configuration information.
     */
    public PlaceConfig getPlaceConfig ();
}
