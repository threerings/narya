//
// $Id: DisplayObjectInfo.java,v 1.4 2002/10/16 01:53:57 ray Exp $

package com.threerings.miso.scene;

import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.ObjectTile;

/**
 * Used to track information about an object in the scene.
 */
public class SceneObject
{
    /** A reference to the object tile itself. */
    public ObjectTile tile;

    /** The x and y tile coordinates of the object. */
    public int x = -1, y = -1;

    /** The object's render priority. */
    public byte priority = 0;

    /** The action associated with this object or null if it has no
     * action. */
    public String action;

    /** The object's bounding rectangle. */
    public Rectangle bounds;

    /**
     * Convenience constructor.
     */
    public SceneObject (int x, int y, ObjectTile tile)
    {
        this.tile = tile;
        this.x = x;
        this.y = y;
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

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof SceneObject) {
            SceneObject oso = (SceneObject)other;
            return (x == oso.x && y == oso.y && tile == oso.tile);
        } else {
            return false;
        }
    }

    // documentation inherited
    public int hashCode ()
    {
        return x ^ y ^ tile.hashCode();
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
