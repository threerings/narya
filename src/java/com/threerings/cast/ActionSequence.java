//
// $Id: ActionSequence.java,v 1.5 2002/07/24 22:24:05 mdb Exp $

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
    /**
     * Defines the name of the default action sequence. When component
     * tilesets are loaded to build a set of composited images for a
     * particular action sequence, a check is first made for a component
     * tileset specific to the action sequence and then for the
     * component's default tileset if the action specific tileset did not
     * exist.
     */
    public static final String DEFAULT_SEQUENCE = "default";

    /** The action sequence name. */
    public String name;

    /** The number of frames per second to show when animating. */
    public float framesPerSecond;

    /** The position of the character's base for this sequence. */
    public Point origin = new Point();

    /** Orientation codes for the orientations available for this
     * action. */
    public int[] orients;

    /**
     * Returns a string representation of this action sequence.
     */
    public String toString ()
    {
        return "[name=" + name + ", framesPerSecond=" + framesPerSecond +
            ", origin=" + origin +
            ", orients=" + (orients == null ? 0 : orients.length) + "]";
    }
}
