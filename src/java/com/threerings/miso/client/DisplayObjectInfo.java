//
// $Id: DisplayObjectInfo.java,v 1.6 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.client;

import java.awt.Rectangle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.miso.data.ObjectInfo;

/**
 * Used to track information about an object in the scene.
 */
public class DisplayObjectInfo extends ObjectInfo
{
    /** A reference to the object tile itself. */
    public ObjectTile tile;

    /** The object's bounding rectangle. This will be filled in
     * automatically by the scene view when the object is used. */
    public Rectangle bounds;

    /**
     * Convenience constructor.
     */
    public DisplayObjectInfo (int tileId, int x, int y)
    {
        super(tileId, x, y);
    }

    /**
     * Convenience constructor.
     */
    public DisplayObjectInfo (ObjectInfo source)
    {
        super(source);
    }

    /**
     * Provides this display object with a reference to its object tile
     * when it becomes available. This must be called before the tile is
     * used in any sort of display circumstances.
     */
    public void setObjectTile (ObjectTile tile)
    {
        this.tile = tile;

        // if we don't already have an overridden render priority; use the
        // one from our object tile (assuming we have one)
        if (priority == 0 && tile != null) {
            priority = (byte)Math.max(
                Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, tile.getPriority()));
        }
    }

    /**
     * Indicate to the scene object that it is being hovered over.
     * 
     * @return true if the object should be repainted.
     */
    public boolean setHovered (boolean hovered)
    {
        return false;
    }
}
