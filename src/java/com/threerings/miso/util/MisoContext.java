//
// $Id: MisoContext.java,v 1.5 2001/10/15 23:53:43 shaper Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;

import com.threerings.media.tile.TileManager;

public interface MisoContext extends Context
{
    /**
     * Returns a reference to the tile manager.  This reference is
     * valid for the lifetime of the application.
     */
    public TileManager getTileManager ();
}
