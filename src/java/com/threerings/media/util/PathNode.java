//
// $Id: PathNode.java,v 1.5 2001/09/05 00:40:33 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Point;

/**
 * A path node is a single destination point in a {@link Path}.
 */
public class PathNode
{
    /** The node coordinates in screen pixels. */
    public Point loc;

    /** The direction to face while heading toward the node. */
    public int dir;

    /**
     * Construct a path node object.
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
