//
// $Id: SceneManagerImpl.java,v 1.4 2001/07/20 08:08:59 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.Log;

import com.samskivert.util.ConfigUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class SceneManagerImpl implements SceneManager
{
    public Scene getScene (int sid)
    {
	// TBD
	return null;
    }

    public String[] getLayerNames ()
    {
	return Scene.XLATE_LAYERS;
    }

    public void loadScenes (String fname)
    {
	try {
	    InputStream tis = ConfigUtil.getStream(fname);
	    if (tis == null) {
		Log.warning("Couldn't find file [fname=" + fname + "].");
		return;
	    }

	    loadScenes(tis);

	} catch (IOException ioe) {
	    Log.warning("Exception loading tileset [fname=" + fname +
			", ioe=" + ioe + "].");
	}
    }
}
