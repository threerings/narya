//
// $Id: ActionSequence.java,v 1.3 2002/03/27 21:51:33 mdb Exp $

package com.threerings.cast;

import java.awt.Point;
import java.io.Serializable;

/**
 * The action sequence class describes a particular character animation
 * sequence. An animation sequence consists of one or more frames of
 * animation, renders at a particular frame rate, and has an origin point
 * that specifies the location of the base of the character in relation to
 * the bounds of the animation images.
 */
public class ActionSequence implements Serializable
{
    /** The action sequence name. */
    public String name;

    /** The number of frames per second to show when animating. */
    public float framesPerSecond;

    /** The position of the character's base for this sequence. */
    public Point origin = new Point();

    /**
     * Returns a string representation of this action sequence.
     */
    public String toString ()
    {
        return "[name=" + name + ", framesPerSecond=" + framesPerSecond +
            ", origin=" + origin + "]";
    }
}
