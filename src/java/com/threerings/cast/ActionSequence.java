//
// $Id: ActionSequence.java,v 1.1 2001/11/01 01:40:42 shaper Exp $

package com.threerings.cast;

import java.awt.Point;

import com.threerings.media.tile.TileSet;

/**
 * The action sequence class describes a single character animation
 * sequence.  An animation sequence may consist of multiple frames of
 * animation, renders at a particular frame rate, and has an origin
 * point that specifies where the base of the character in the
 * animation sequence is to be placed.
 */
public class ActionSequence
{
    /** The unique action sequence identifier. */
    public int asid;

    /** The action sequence name. */
    public String name;

    /** The file id specifier for the tile set image file name. */
    public String fileid;

    /** The tile set description for this sequence.  Intended for
     * cloning with an image path to reference an actual set of tile
     * images suiting the action sequence.  */
    public TileSet tileset;

    /** The number of frames per second to show when animating. */
    public int fps;

    /** The position of the character's base for this sequence. */
    public Point origin = new Point();

    /**
     * Returns a string representation of this action sequence.
     */
    public String toString ()
    {
        return "[asid=" + asid + ", name=" + name +
            ", fps=" + fps + ", origin=" + origin + "]";
    }
}
