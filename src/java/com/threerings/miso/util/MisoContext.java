//
// $Id: MisoContext.java,v 1.6 2001/11/02 03:09:10 shaper Exp $

package com.threerings.miso.util;

import com.samskivert.util.Context;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileManager;

public interface MisoContext extends Context
{
    /**
     * Returns a reference to the image manager.  This reference is
     * valid for the lifetime of the application.
     */
    public ImageManager getImageManager ();

    /**
     * Returns a reference to the tile manager.  This reference is
     * valid for the lifetime of the application.
     */
    public TileManager getTileManager ();
}
