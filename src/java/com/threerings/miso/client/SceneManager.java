//
// $Id: SceneManager.java,v 1.6 2001/07/23 22:31:47 shaper Exp $

package com.threerings.miso.scene;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Manages the various scenes that are displayed during the game and
 * provides simplified retrieval and caching facilities.
 */
public interface SceneManager
{
    /**
     * Return an ArrayList containing all Scene objects available.
     *
     * @return the list of scenes.
     */
    public ArrayList getAllScenes ();

    /**
     * Return a new Scene object with the next unique scene id.  The
     * scene id is only guaranteed to be unique in the context of the
     * scenes currently available via this SceneManager.
     */
    public Scene getNewScene ();

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
