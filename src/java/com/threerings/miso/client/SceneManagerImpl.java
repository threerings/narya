//
// $Id: SceneManagerImpl.java,v 1.2 2001/07/17 17:21:33 shaper Exp $

package com.threerings.cocktail.miso.scene;

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
