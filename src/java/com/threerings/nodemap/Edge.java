//
// $Id: Edge.java,v 1.3 2002/06/15 02:13:11 shaper Exp $

package com.threerings.nodemap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;

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
     * Sets the color used to render the edge, or <code>null</code> (the
     * default) to use the current graphics context color.
     */
    public void setColor (Color color)
    {
        _color = color;
    }

    /**
     * Sets the stroke used to render the edge, or <code>null</code> (the
     * default) to use the current graphics context stroke.
     */
    public void setStroke (Stroke stroke)
    {
        _stroke = stroke;
    }

    /**
     * Return a string representation of this edge.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer("[");
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

    /** The edge color. */
    protected Color _color;

    /** The edge stroke. */
    protected Stroke _stroke;
}
