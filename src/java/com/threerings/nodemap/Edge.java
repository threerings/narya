//
// $Id: Edge.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

import java.awt.Graphics;

/**
 * The edge class represents an edge connecting two nodes.
 */
public abstract class Edge
{
    /** The source node. */
    public Node src;

    /** The destination node. */
    public Node dst;

    /**
     * Construct an edge object connecting the given nodes.
     *
     * @param src the source node.
     * @param dst the destination node.
     */
    public Edge (Node src, Node dst)
    {
	this.src = src;
	this.dst = dst;
    }

    /**
     * Paint the edge to the given graphics context.
     *
     * @param g the graphics context.
     */
    public abstract void paint (Graphics g);

    /**
     * Return a string representation of this edge.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[");
	toString(buf);
	return buf.append("]").toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific event information to the string buffer.
     */
    public void toString (StringBuffer buf)
    {
	buf.append("src=").append(src);
	buf.append(", dst=").append(dst);
    }
}
