//
// $Id: RuntimeScene.java,v 1.2 2002/06/20 22:10:55 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.data.PlaceConfig;

/**
 * This interface makes available the scene information that is needed by
 * the server to manage a scene. At this basic level, not much information
 * is available, but extensions to this interface begin to create a more
 * comprehensive picture of a scene in a system built from the Whirled
 * services.
 *
 * <p> Additionally, at this basic level, the <code>RuntimeScene</code>
 * does not differ greatly (or at all) from the {@link
 * com.threerings.whirled.client.DisplayScene} interface, but the
 * distinction provides a mechanism for handling the more substantial
 * differences that appear in more sophisticated extensions to the base
 * scene services.
 */
public interface RuntimeScene
{
    /**
     * Returns the unique identifier for this scene.
     */
    public int getId ();

    /**
     * Returns the human readable name of this scene.
     */
    public String getName ();

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
