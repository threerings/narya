//
// $Id: MisoContext.java,v 1.7 2001/11/18 04:09:23 mdb Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileManager;

public interface MisoContext extends Context
{
    /**
     * Returns a reference to the tile manager.  This reference is
     * valid for the lifetime of the application.
     */
    public TileManager getTileManager ();
}
