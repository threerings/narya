//
// $Id: SceneParser.java,v 1.1 2001/07/23 22:45:04 shaper Exp $

package com.threerings.miso.scene;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The SceneParser is a general interface to be implemented by classes
 * that load scene descriptions in a particular format from a file.
 */
public interface SceneParser
{
    /**
     * Read scene description data from the given file and construct
     * Scene objects to suit.  Return an ArrayList of all Scene
     * objects constructed, or a zero-length array if no scene
     * descriptions were fully parsed.
     */
    public ArrayList loadScenes (String fname) throws IOException;
}
