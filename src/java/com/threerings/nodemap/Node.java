//
// $Id: Node.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

import java.awt.Graphics;
import java.awt.Point;
import java.util.*;

/**
 * The node class represents a single node in a directed graph of nodes.
 *
 * @see NodeMap
 */
public abstract class Node
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

	_pos = new Point();
	_edges = new ArrayList();
    }

    /**
     * Add a connecting edge to this node.  An edge may not be
     * added to the node more than once.
     *
     * @param e the new edge.
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
     * Return an <code>Iterator</code> object that iterates over the
     * edges leaving this node.
     *
     * @return the iterator object.
     */
    public Iterator getEdges ()
    {
	return (Collections.unmodifiableList(_edges)).iterator();
    }

    /**
     * Return the node's current position in pixels.
     *
     * @return point the node position.
     */
    public Point getPosition ()
    {
	return _pos;
    }

    /**
     * Set the node's position in pixels.
     *
     * @param x the x-position.
     * @param y the y-position.
     */
    public void setPosition (int x, int y)
    {
	_pos.setLocation(x, y);
    }

    /**
     * Return whether the node contains the given point.
     *
     * @param x the x-position.
     * @param y the y-position.
     *
     * @return true if the node contains the point, false if not.
     */
    public boolean contains (int x, int y)
    {
	return (x >= _pos.x && x <= (_pos.x + _width) &&
		y >= _pos.y && y <= (_pos.y + _height));
    }

    /**
     * Return the node width in pixels.
     */
    public int getWidth ()
    {
	return _width;
    }

    /**
     * Return the node height in  pixels.
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
     * Handle mouse-entered events for this node.
     */
    public void handleMouseEntered ()
    {
	Log.info("handleMouseEntered [n=" + this + "].");
    }

    /**
     * Handle mouse-exited events for this node.
     */
    public void handleMouseExited ()
    {
	Log.info("handleMouseExited [n=" + this + "].");
    }

    /**
     * Handle mouse-clicked events for this node.
     *
     * @param x the x-coordinate in intra-node pixels.
     * @param y the y-coordinate in intra-node pixels.
     */
    public void handleMouseClicked (int x, int y)
    {
	Log.info("handleMouseClicked [n=" + this + "].");
    }

    /**
     * Return a string representation of this node.
     */
    public String toString ()
    {
	return _name;
    }

    /** The position in pixel coordinates. */
    protected Point _pos;

    /** The dimensions in pixel coordinates. */
    protected int _width, _height;

    /** The node name. */
    protected String _name;

    /** The edges leaving this node. */
    protected ArrayList _edges;
}
