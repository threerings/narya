//
// $Id: ViewerModel.java,v 1.2 2001/09/05 00:45:27 shaper Exp $

package com.threerings.miso.viewer;

import com.samskivert.util.Config;

public class ViewerModel
{
    /** The config key prefix for miso viewer properties. */
    public static final String CONFIG_KEY = "miso-viewer";

    /** The filename of the scene to display. */
    public String scenefile;

    public ViewerModel (Config config)
    {
        scenefile = config.getValue(SCENE_KEY, DEF_SCENE);
    }

    /** The config key to obtain the default scene filename. */
    protected static final String SCENE_KEY =
	CONFIG_KEY + ".default_scene";

    /** The default scene to load and display. */
    protected static final String DEF_SCENE = "rsrc/scenes/default.xml";
}
