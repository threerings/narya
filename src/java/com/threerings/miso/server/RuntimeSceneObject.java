//
// $Id: RuntimeSceneObject.java,v 1.1 2002/12/11 23:07:21 shaper Exp $

package com.threerings.miso.server;

import com.samskivert.util.StringUtil;

/**
 * Used to track server-side information about an object in a miso scene.
 */
public class RuntimeSceneObject
{
    /** The x and y tile coordinates of the object. */
    public int x = -1, y = -1;

    /** The action associated with this object or null if it has no
     * action. */
    public String action;

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof RuntimeSceneObject) {
            RuntimeSceneObject oso = (RuntimeSceneObject)other;
            // TODO: we should probably check the tile type as well, but
            // for now we only differentiate runtime miso scene objects by
            // their coordinates within the scene since we don't bother
            // keeping around tile information server-side
            return (x == oso.x && y == oso.y);
        } else {
            return false;
        }
    }

    // documentation inherited
    public int hashCode ()
    {
        return x ^ y;
    }

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
