//
// $Id: DisplayObjectInfo.java,v 1.7 2003/02/04 03:33:09 mdb Exp $

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
    }

    /**
     * Returns the render priority of this object tile.
     */
    public int getPriority ()
    {
        // if we have no specified priority return our object tile's
        // default priority
        return (priority == 0 && tile != null) ? tile.getPriority() : priority;
    }

    /**
     * Overrides the render priority of this object.
     */
    public void setPriority (byte priority)
    {
        this.priority = (byte)Math.max(
            Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, priority));
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
