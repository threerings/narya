//
// $Id: RecolorableTileSet.java,v 1.1 2004/08/30 22:09:29 ray Exp $

package com.threerings.media.tile;

/**
 * Indicates that a tileset has recolorization classes defined.
 */
public interface RecolorableTileSet
{
    /**
     * Returns the colorization classes that should be used to recolor
     * objects in this tileset.
     */
    public String[] getColorizations ();
}
