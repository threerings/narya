//
// $Id: Traverser.java,v 1.4 2001/11/27 22:17:42 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.miso.tile.BaseTile;

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
    public boolean canTraverse (BaseTile tile);
}
