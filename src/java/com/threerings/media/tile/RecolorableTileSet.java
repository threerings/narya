//
// $Id$

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
