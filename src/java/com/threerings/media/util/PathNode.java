//
// $Id: PathNode.java,v 1.2 2001/08/02 20:43:03 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.Point;

/**
 * The PathNode object is a single destination point in a Path.
 */
public class PathNode
{
    /** The node coordinates in screen pixels. */
    public Point loc;

    /** The direction to face while heading toward the node. */
    public int dir;

    /**
     * Construct a PathNode object.
     *
     * @param x the node x-position.
     * @param y the node y-position.
     * @param dir the facing direction.
     */
    public PathNode (int x, int y, int dir)
    {
        loc = new Point(x, y);
        this.dir = dir;
    }

    /**
     * Return a string representation of this path node.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[x=").append(loc.x);
        buf.append(", y=").append(loc.y);
        buf.append(", dir=").append(dir);
        return buf.append("]").toString();
    }
}
