//
// $Id: NodeMap.java,v 1.2 2001/08/23 00:22:30 shaper Exp $

package com.threerings.nodemap;

import java.awt.*;
import java.util.*;
import javax.swing.JComponent;

import com.samskivert.swing.ToolTipManager;

/**
 * The node map class represents a directed graph of nodes comprised
 * of {@link Node} objects.  A user-specifiable {@link LayoutManager}
 * is used to lay out the nodes in the graph.
 */
public class NodeMap
{
    /**
     * Construct a node map object with the given layout manager.
     *
     * @param lgmr the layout manager.
     */
    public NodeMap (LayoutManager lmgr)
    {
	_lmgr = lmgr;
	_nodes = new ArrayList();
	_dirty = false;
    }

    /**
     * Force the nodes in the map to be re-laid out.
     */
    public void layout ()
    {
	// do nothing if we have no nodes
	if (_nodes.size() == 0) return;

	// default to using the first node as the root
	if (_root == null) {
	    _root = (Node)_nodes.get(0);
	}

	// lay out the nodes
	_lmgr.layout(_root, _nodes);

	// update bounding node positions
	updateBounds();

	// note that we're no longer dirty
	_dirty = false;
    }

    /**
     * Calculate the bounding rectangle that wholly contains the node map.
     */
    protected void updateBounds ()
    {
	Iterator iter = _nodes.iterator();
	while (iter.hasNext()) {
	    Node n = (Node)iter.next();
	    Point pos = n.getPosition();
	    int wid = n.getWidth(), hei = n.getHeight();

	    _minx = Math.min(_minx, pos.x);
	    _maxx = Math.max(_maxx, pos.x + wid);
	    _miny = Math.min(_miny, pos.y);
	    _maxy = Math.max(_maxy, pos.y + hei);
	}
    }	

    /**
     * Render the node map to the given graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g)
    {
	// lay out the nodes if necessary
	if (_dirty) {
	    layout();
	}

	// translate context to node map origin
	g.translate(-_minx, -_miny);

	// render all node edges
	int size = _nodes.size();
	for (int ii = 0; ii < size; ii++) {
	    Node n = (Node)_nodes.get(ii);
	    n.paintEdges(g);
	}

	// render all nodes
	for (int ii = 0; ii < size; ii++) {
	    Node n = (Node)_nodes.get(ii);
	    n.paint(g);
	}

	// highlight the last node entered
	if (_lastnode != null) {
	    g.setColor(Color.red);
	    Point pos = _lastnode.getPosition();
	    int wid = _lastnode.getWidth(), hei = _lastnode.getHeight();
	    g.drawRect(pos.x, pos.y, wid, hei);
	}

	// draw the tool tip, if any
	if (_tip != null) {
	    _tip.paintToolTip(g);
	}

	// restore original origin
	g.translate(_minx, _miny);
    }

    /**
     * Add the given node to the node map.  A given node may not be
     * present in the node map more than once.
     *
     * @param n the node to add.
     */
    public void addNode (Node n) throws DuplicateNodeException
    {
	// don't allow adding a node more than once
	if (_nodes.contains(n)) {
	    throw new DuplicateNodeException();
	}

	// add the node
	_nodes.add(n);
	_dirty = true;
    }

    /**
     * Add the given edge to the node.  Edges may not be added to a
     * node more than once, or to a node that is not present in the
     * node map.
     *
     * @param n the node.
     * @param e the edge to add.
     */
    public void addEdge (Node n, Edge e)
	throws NoSuchNodeException, DuplicateEdgeException
    {
	// don't allow adding an edge to an unknown node
	if (!_nodes.contains(n)) {
	    throw new NoSuchNodeException();
	}

	// add the edge to the node
	n.addEdge(e);
	_dirty = true;
    }

    /**
     * Set the root node for the node map.  The given node must be
     * present in the node map.
     *
     * @param n the root node.
     */
    public void setRootNode (Node n) throws NoSuchNodeException
    {
	if (!_nodes.contains(n)) {
	    throw new NoSuchNodeException();
	}

	_root = n;
	_dirty = true;
    }

    /**
     * Return the dimensions of the node map in pixels.
     */
    public Dimension getSize ()
    {
	// lay out the nodes if necessary
	if (_dirty) {
	    layout();
	}

	return new Dimension(_maxx - _minx, _maxy - _miny);
    }

    /**
     * Handle mouse-moved events passed on by the containing panel.
     * Inform any affected nodes of mouse-entered and mouse-exited
     * events.
     *
     * @param x the mouse x-coordinate.
     * @param y the ymouse y-coordinate.
     */
    public void handleMouseMoved (int x, int y)
    {
	Node enternode = null;

	// tell the tip manager that the mouse moved
	if (_tipmgr != null) {
	    _tipmgr.handleMouseMoved();
	}

	// translate coordinates to node map origin
	x += _minx;
	y += _miny;

	// check whether we've entered a node
	int size = _nodes.size();
	for (int ii = 0; ii < size; ii++) {
	    Node n = (Node)_nodes.get(ii);

	    if (n.contains(x, y)) {

		// bail if we're in the same node we were in last
		if (_lastnode == n) return;

		// note that we're entering this node
		enternode = n;

		break;
	    }
	}

	// exit the last node, if any
	if (_lastnode != null) {
	    _lastnode.handleMouseExited();

	    if (_tipmgr != null) {
		_tipmgr.handleMouseExited(_lastnode);
	    }
	}

	// enter the new node, if any
	if (enternode != null) {
	    enternode.handleMouseEntered();

	    if (_tipmgr != null) {
		_tipmgr.handleMouseEntered(enternode);
	    }
	}

	// update the last node entered
	_lastnode = enternode;
    }

    /**
     * Handle mouse-clicked events passed on by the containing panel.
     * Inform any affected nodes of mouse-clicked events.
     *
     * @param x the mouse x-coordinate.
     * @param y the ymouse y-coordinate.
     */
    public void handleMouseClicked (int x, int y)
    {
	// translate coordinates to node map origin
	x += _minx;
	y += _miny;

	int size = _nodes.size();
	for (int ii = 0; ii < size; ii++) {
	    Node n = (Node)_nodes.get(ii);

	    if (n.contains(x, y)) {

		// inform the node of the mouse click, with
		// coordinates translated to suit node interior
		Point pos = n.getPosition();
		n.handleMouseClicked(x - pos.x, y - pos.y);

		// let the tool tip manager know about the click
		if (_tipmgr != null) {
		    _tipmgr.handleMouseClicked(n);
		}

		// nodes can't overlap for now, so we're done
		return;
	    }
	}
    }

    /**
     * Set the node whose tool tip should be displayed when the node
     * map is rendered.
     *
     * @param node the tool tip node.
     */
    public void setToolTipNode (Node node)
    {
	_tip = node;
    }

    /**
     * Set the tool tip manager that the node map should notify of
     * node enter, exit, and clicked events.
     *
     * @param tipmgr the tool tip manager.
     */
    public void setToolTipManager (ToolTipManager tipmgr)
    {
	_tipmgr = tipmgr;
    }

    /** The node whose tool tip is currently displayed. */
    protected Node _tip;

    /** The tool tip manager. */
    protected ToolTipManager _tipmgr;

    /** The layout manager. */
    protected LayoutManager _lmgr;

    /** The nodes in the node map. */
    protected ArrayList _nodes;

    /** The root node. */
    protected Node _root;

    /** The bounding box coordinates. */
    protected int _minx, _maxx, _miny, _maxy;

    /** The last node the mouse entered. */
    protected Node _lastnode;

    /** Whether the node map needs to be laid out. */
    protected boolean _dirty;
}
