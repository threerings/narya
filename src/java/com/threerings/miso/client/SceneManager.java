//
// $Id: SceneManager.java,v 1.4 2001/07/20 08:08:59 shaper Exp $

package com.threerings.miso.scene;

import java.io.IOException;
import java.io.InputStream;

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

    /**
     * Load all scene objects described in the specified file into the
     * set of available scenes.
     */
    public void loadScenes (String fname);

    /**
     * Load all scene objects described in the specified input stream
     * into the set of available scenes.
     */
    public void loadScenes (InputStream in) throws IOException;
}
