//
// $Id: SceneManagerImpl.java,v 1.5 2001/07/23 18:52:51 shaper Exp $

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
}
