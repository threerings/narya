//
// $Id: DisplayMisoScene.java,v 1.10 2003/02/12 07:21:15 mdb Exp $

package com.threerings.miso.client;

import com.threerings.media.tile.Tile;
import com.threerings.miso.data.MisoScene;
import com.threerings.miso.tile.BaseTile;

/**
 * Extends the {@link MisoScene} with functionality needed only by
 * entities that plan to display a miso scene.
 */
public interface DisplayMisoScene extends MisoScene
{
    /**
     * Returns the base tile at the specified coordinates.
     */
    public BaseTile getBaseTile (int x, int y);

    /**
     * Returns the fringe tile at the specified coordinates.
     */
    public Tile getFringeTile (int x, int y);

    /**
     * Returns true if the supplied traverser can traverse the specified
     * tile coordinate. The traverser is whatever object is passed along
     * to the path finder when a path is being computed. Scene
     * implementations which support custom traversal based on the type of
     * the traverser will want to reflect the traverser's class and act
     * acordingly.
     */
    public boolean canTraverse (Object traverser, int x, int y);
}
