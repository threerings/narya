//
// $Id: Traverser.java,v 1.2 2001/08/16 23:14:21 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.tile.Tile;

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
