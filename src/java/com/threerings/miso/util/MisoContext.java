//
// $Id: MisoContext.java,v 1.8 2002/03/28 22:32:32 mdb Exp $

package com.threerings.miso.util;

import com.threerings.media.tile.TileManager;

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
    public TileManager getTileManager ();
}
