//
// $Id: SceneManager.java,v 1.3 2001/07/18 21:45:42 shaper Exp $

package com.threerings.miso.scene;

/**
 * Manages the various scenes that are displayed during the game and
 * provides simplified retrieval and caching facilities.
 */
public interface SceneManager
{
    /**
     * Return the Scene object for the specified scene id.
     */
    public Scene getScene (int sid);

    /**
     * Return a String array of all layer names ordered by ascending
     * layer id.
     */
    public String[] getLayerNames ();
}
