//
// $Id: Node.java,v 1.7 2002/06/15 01:59:25 shaper Exp $

package com.threerings.nodemap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.samskivert.swing.ToolTipProvider;
import com.samskivert.swing.util.SwingUtil;

/**
 * The node class represents a single node in a directed graph of nodes.
 *
 * @see NodeMap
 */
public abstract class Node implements ToolTipProvider
{
    /**
     * Construct a node with the given attributes.
     *
     * @param name the node name.
     * @param width the width in pixels.
     * @param height the height in pixels.
     */
    public Node (String name, int width, int height)
    {
	_name = name;
	_width = width;
	_height = height;

	_edges = new ArrayList();
    }

    /**
     * Add a connecting edge to this node.  An edge may not be
     * added to the node more than once.
     *
     * @param e the new edge.
     *
     * @exception DuplicateEdgeException thrown if the node already
     * contains the given edge.
     */
    protected void addEdge (Edge e) throws DuplicateEdgeException
    {
	// don't allow adding the same edge object twice
	if (_edges.contains(e)) {
	    throw new DuplicateEdgeException();
	}

	// save off the new connecting edge
	_edges.add(e);
    }

    /**
     * Returns an <code>Iterator</code> object that iterates over the
     * edges leaving this node.
     *
     * @return the iterator object.
     */
    public Iterator getEdges ()
    {
	return (Collections.unmodifiableList(_edges)).iterator();
    }

    /**
     * Returns the node's x position in screen coordinates.
     */
    public int getX ()
    {
	return _x;
    }


    /**
     * Returns the node's y position in screen coordinates.
     */
    public int getY ()
    {
	return _y;
    }

    /**
     * Set the node's position in pixels.
     *
     * @param x the x-position.
     * @param y the y-position.
     */
    public void setLocation (int x, int y)
    {
	_x = x;
	_y = y;
    }

    /**
     * Returns whether the node contains the given point.
     *
     * @param x the x-position.
     * @param y the y-position.
     *
     * @return true if the node contains the point, false if not.
     */
    public boolean contains (int x, int y)
    {
	return (x >= _x && x <= (_x + _width) &&
		y >= _y && y <= (_y + _height));
    }

    /**
     * Returns the node width in pixels.
     */
    public int getWidth ()
    {
	return _width;
    }

    /**
     * Returns the node height in  pixels.
     */
    public int getHeight ()
    {
	return _height;
    }

    /**
     * Render the node to the given graphics context.
     *
     * @param g the graphics context.
     */
    public abstract void paint (Graphics g);

    /**
     * Render the node's connecting edges to the given graphics context.
     *
     * @param g the graphics context.
     */
    public void paintEdges (Graphics g)
    {
	int size = _edges.size();
	for (int ii = 0; ii < size; ii++) {
	    ((Edge)_edges.get(ii)).paint(g);
	}
    }

    /**
     * Render a tool tip for this node to the given graphics context.
     * By default, the tool tip is a yellow box containing the node's
     * name.
     *
     * @param g the graphics context.
     * @param x the x-position at which the tip should be drawn.
     * @param y the y-position at which the tip should be drawn.
     */
    public void paintToolTip (Graphics g, int x, int y)
    {
	Dimension d = getToolTipSize(g);

	// draw the yellow box
	g.setColor(Color.yellow);
	g.fillRect(x, y, d.width, d.height);
	g.setColor(Color.black);
	g.drawRect(x, y, d.width, d.height);

	// draw the node name
	SwingUtil.drawStringCentered(g, _name, x, y, d.width, d.height);
    }

    public Dimension getToolTipSize (Graphics g)
    {
	// calculate tool tip dimensions
	FontMetrics fm = g.getFontMetrics(g.getFont());

	int wid = fm.stringWidth(_name) + 10;
	int hei = fm.getAscent() + 4;

	return new Dimension(wid, hei);
    }

    /**
     * Handle mouse-entered events for this node.
     */
    public void handleMouseEntered () { }

    /**
     * Handle mouse-exited events for this node.
     */
    public void handleMouseExited () { }

    /**
     * Handle mouse-clicked events for this node.
     *
     * @param x the x-coordinate in intra-node pixels.
     * @param y the y-coordinate in intra-node pixels.
     */
    public void handleMouseClicked (int x, int y) { }

    /**
     * Returns a string representation of this node.
     */
    public String toString ()
    {
	return _name;
    }

    /** The position in pixel coordinates. */
    protected int _x, _y;

    /** The dimensions in pixel coordinates. */
    protected int _width, _height;

    /** The node name. */
    protected String _name;

    /** The edges leaving this node. */
    protected ArrayList _edges;
}
