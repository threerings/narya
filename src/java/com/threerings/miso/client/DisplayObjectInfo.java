//
// $Id: DisplayObjectInfo.java,v 1.2 2002/09/23 21:54:50 mdb Exp $

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

    /** The object's index in the scene object list. */
    public int index = -1;

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
