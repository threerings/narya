//
// $Id: EditableSceneManager.java,v 1.6 2001/07/23 22:31:47 shaper Exp $

package com.threerings.miso.scene;

import java.io.IOException;
import java.util.ArrayList;

import com.threerings.miso.Log;

/**
 * The EditableSceneManager extends general scene manager
 * functionality to allow reading scenes from and writing scenes to
 * XML files.
 */
public class EditableSceneManager extends SceneManagerImpl
{
    /**
     * Load all scenes described in the specified XML file into the
     * hashtable of scenes currently available.
     *
     * @param fname the file to load scenes from.
     */
    public void loadScenes (String fname) throws IOException
    {
        ArrayList scenes = null;
        _sid = 0;
        try {
            scenes = new XMLSceneParser().loadScenes(fname);
        } catch (IOException ioe) {
	    Log.warning("Exception loading scenes [fname=" + fname +
			", ioe=" + ioe + "].");
            return;
        }

        // bail if we didn't find any scenes
        int size = scenes.size();
        if (size == 0) {
            Log.warning("No scenes found [fname=" + fname + "].");
            return;
        }

        // copy new scenes into the main scene hashtable
        for (int ii = 0; ii < size; ii++) {
            Scene scene = (Scene)scenes.get(ii);
            _scenes.put(scene.getId(), scene);
            Log.info("Adding scene to cache [scene=" + scene + "].");
        }
    }

    /**
     * Write all scenes currently available to the specified file in
     * XML format.
     *
     * @param fname the file to write the scenes to.
     */
    public void writeAllScenes (String fname)
    {
        try {
            ArrayList scenes = getAllScenes();
            new XMLSceneWriter(scenes).writeToFile(fname);

        } catch (IOException ioe) {
            Log.warning("Exception writing scenes to file [fname=" +
                        fname + ", ioe=" + ioe + "].");
        }
    }
}
