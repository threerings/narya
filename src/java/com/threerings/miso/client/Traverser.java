//
// $Id: Traverser.java,v 1.1 2001/08/15 02:30:27 shaper Exp $

package com.threerings.miso.tile;

/**
 * The <code>Traverser</code> interface should be implemented by
 * sprites that are going to traverse some path in a scene.  This
 * allows path determination to take into account any special
 * abilities the traverser may have that alter the traversability of
 * tiles.
 */
public interface Traverser
{
    /**
     * Return whether the traverser can traverse the specified tile.
     *
     * @param tile the tile to traverse.
     *
     * @return whether the tile is traversable.
     */
    public boolean canTraverse (Tile tile);
}
