//
// $Id: Edge.java,v 1.4 2004/08/27 02:20:11 mdb Exp $
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
