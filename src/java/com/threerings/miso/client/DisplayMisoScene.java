//
// $Id: DisplayMisoScene.java,v 1.5 2002/04/27 18:41:14 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.util.Iterator;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.miso.tile.BaseTile;

/**
 * Makes available the information from the {@link MisoSceneModel} in a
 * form that is amenable to actually displaying the scene in a user
 * interface. As with all display scene implementations, the information
 * provided is read-only and should never be modified by the caller.
 */
public interface DisplayMisoScene
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
     * Returns an iterator over all object tiles in this scene.
     */
    public Iterator getObjectTiles ();

    /**
     * Returns the tile coordinates for the specified object tile.
     *
     * @param tile the tile for which coordinates are to be fetched; this
     * tile must have been obtained from a call to {@link
     * #getObjectTiles}.
     */
    public Point getObjectCoords (ObjectTile tile);

    /**
     * Returns the action associated with the specified object tile. Null
     * is returned if the object tile does not have an associated action.
     *
     * @param tile the tile for which the action is to be fetched; this
     * tile must have been obtained from a call to {@link
     * #getObjectTiles}.
     */
    public String getObjectAction (ObjectTile tile);
}
