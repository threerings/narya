//
// $Id: MisoContext.java,v 1.1 2001/07/20 23:42:20 shaper Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;
import com.threerings.miso.tile.TileManager;
import com.threerings.miso.scene.SceneManager;

public interface MisoContext extends Context
{
    /**
     * Return a reference to the TileManager.
     */
    public TileManager getTileManager ();

    /**
     * Return a reference to the SceneManager.
     */
    public SceneManager getSceneManager ();
}
