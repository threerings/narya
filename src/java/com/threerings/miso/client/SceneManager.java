//
// $Id: SceneManager.java,v 1.2 2001/07/16 22:12:01 shaper Exp $

package com.threerings.cocktail.miso.scene;

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
