//
// $Id: SceneManagerImpl.java,v 1.3 2001/07/18 21:45:42 shaper Exp $

package com.threerings.miso.scene;

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
