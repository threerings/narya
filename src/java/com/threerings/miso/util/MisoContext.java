//
// $Id: MisoContext.java,v 1.2 2001/07/21 01:51:10 shaper Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;
import com.threerings.miso.tile.TileManager;
import com.threerings.miso.scene.SceneManager;

public interface MisoContext extends Context
{
    /**
     * Return a reference to the TileManager.  This reference is valid
     * for the lifetime of the application.
     */
    public TileManager getTileManager ();

    /**
     * Return a reference to the SceneManager.  This reference is
     * valid for the lifetime of the application.
     */
    public SceneManager getSceneManager ();
}
