//
// $Id: MisoContext.java,v 1.10 2003/04/17 19:21:16 mdb Exp $

package com.threerings.miso.util;

import com.threerings.media.FrameManager;
import com.threerings.miso.tile.MisoTileManager;

/**
 * Provides Miso code with access to the managers that it needs to do its
 * thing.
 */
public interface MisoContext
{
    /**
     * Returns the frame manager that our scene panel will interact with.
     */
    public FrameManager getFrameManager ();

    /**
     * Returns a reference to the tile manager. This reference is valid
     * for the lifetime of the application.
     */
    public MisoTileManager getTileManager ();
}
