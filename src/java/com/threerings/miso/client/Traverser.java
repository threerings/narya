//
// $Id: Traverser.java,v 1.3 2001/10/08 21:04:25 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.tile.MisoTile;

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
    public boolean canTraverse (MisoTile tile);
}
