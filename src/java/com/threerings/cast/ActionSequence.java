//
// $Id: ActionSequence.java,v 1.8 2004/08/27 02:12:25 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
