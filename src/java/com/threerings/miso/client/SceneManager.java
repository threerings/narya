//
// $Id: SceneManager.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.scene;

/**
 * Manages the various scenes that are displayed during the game and
 * provides simplified retrieval and caching facilities.
 */
public interface SceneManager
{
    public Scene getScene (String name);

    public Scene getScene (int sid);
}
