//
// $Id: ComponentType.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

import java.awt.Point;

/**
 * The component type class contains the base information necessary
 * for a variety of {@link CharacterComponent} objects to be usefully
 * composited into animation sequences representing a character.
 */
public class ComponentType
{
    /** The unique component type identifier. */
    public int ctid;

    /** The number of animation frames. */
    public int frameCount;

    /** The number of frames per second to show when animating. */
    public int fps;

    /** The origin of the component type's base. */
    public Point origin = new Point();

    /**
     * Returns a string representation of this component type.
     */
    public String toString ()
    {
        return "[ctid=" + ctid + ", frameCount=" + frameCount +
            ", fps=" + fps + ", origin=" + origin + "]";
    }
}
