//
// $Id: MisoContext.java,v 1.9 2002/04/06 02:04:23 mdb Exp $

package com.threerings.miso.util;

import com.threerings.miso.tile.MisoTileManager;

/**
 * Provides Miso code with access to the managers that it needs to do its
 * thing.
 */
public interface MisoContext
{
    /**
     * Returns a reference to the tile manager. This reference is valid
     * for the lifetime of the application.
     */
    public MisoTileManager getTileManager ();
}
