//
// $Id: MisoContext.java,v 1.3 2001/08/15 00:00:51 mdb Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;
import com.threerings.miso.tile.TileManager;

public interface MisoContext extends Context
{
    /**
     * Return a reference to the TileManager.  This reference is valid
     * for the lifetime of the application.
     */
    public TileManager getTileManager ();
}
