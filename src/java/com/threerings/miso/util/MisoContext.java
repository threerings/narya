//
// $Id: MisoContext.java,v 1.4 2001/08/16 23:14:21 mdb Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;
import com.threerings.media.tile.TileManager;

public interface MisoContext extends Context
{
    /**
     * Return a reference to the TileManager.  This reference is valid
     * for the lifetime of the application.
     */
    public TileManager getTileManager ();
}
