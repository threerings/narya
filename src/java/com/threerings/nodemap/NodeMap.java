//
// $Id: NodeMap.java,v 1.5 2001/10/11 00:41:27 shaper Exp $

package com.threerings.nodemap;

import java.awt.*;
import java.util.*;
import javax.swing.JComponent;

import com.samskivert.swing.ToolTipManager;
import com.samskivert.swing.ToolTipProvider;
import com.samskivert.swing.util.ToolTipUtil;

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
	if (_nodes.size() == 0) {
	    return;
	}

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
     * Calculate the bounding rectangle that wholly contains the node
     * map.
     */
    protected void updateBounds ()
    {
	Iterator iter = _nodes.iterator();
	while (iter.hasNext()) {
	    Node n = (Node)iter.next();
	    int x = n.getX(), y = n.getY();
	    int wid = n.getWidth(), hei = n.getHeight();

	    _minx = Math.min(_minx, x);
	    _maxx = Math.max(_maxx, x + wid);
	    _miny = Math.min(_miny, y);
	    _maxy = Math.max(_maxy, y + hei);
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
	    int x = _lastnode.getX(), y = _lastnode.getY();
	    int wid = _lastnode.getWidth(), hei = _lastnode.getHeight();
	    g.drawRect(x, y, wid, hei);
	}

	// draw the tool tip, if any
	if (_tipper != null) {
	    // determine proper tool tip placement
	    Point pos = ToolTipUtil.getTipPosition(
		_tipx, _tipy, _tipper.getToolTipSize(g), getBounds());

	    // paint away
	    _tipper.paintToolTip(g, pos.x, pos.y);
	}

	// restore original origin
	g.translate(_minx, _miny);
    }

    /**
     * Add the given node to the node map.  A given node may not be
     * present in the node map more than once.
     *
     * @param n the node to add.
     *
     * @exception DuplicateNodeException thrown if the node already
     * exists in the node map.
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
     *
     * @exception NoSuchNodeException thrown if the given node does
     * not exist in the node map.
     * @exception DuplicateEdgeException thrown if the given node
     * already contains the given edge.
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
     *
     * @exception NoSuchNodeException thrown if the given node does
     * not exist in the node map.
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
     * Returns the dimensions of the node map in pixels.
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
     * Returns the bounding rectangle of the node map.
     */
    public Rectangle getBounds ()
    {
	Dimension size = getSize();
	return new Rectangle(_minx, _miny, size.width, size.height);
    }

    /**
     * Handle mouse-moved events passed on by the containing panel.
     * Inform any affected nodes of mouse-entered and mouse-exited
     * events.
     *
     * @param x the mouse x-coordinate.
     * @param y the mouse y-coordinate.
     */
    public void handleMouseMoved (int x, int y)
    {
	Node enternode = null;

	// translate coordinates to node map origin
	x += _minx;
	y += _miny;

	// tell the tip manager that the mouse moved
	if (_tipmgr != null) {
	    _tipmgr.handleMouseMoved(x, y);
	}

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
		_tipmgr.handleMouseExited();
	    }
	}

	// enter the new node, if any
	if (enternode != null) {
	    enternode.handleMouseEntered();

	    if (_tipmgr != null) {
		_tipmgr.handleMouseEntered(enternode, x, y);
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
     * @param y the mouse y-coordinate.
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
		int nx = n.getX(), ny = n.getY();
		n.handleMouseClicked(x - nx, y - ny);

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
     * Handle mouse-exited events passed on by the containing panel.
     * Inform the tool tip manager that we've exited any node we may
     * have been within.
     */
    public void handleMouseExited ()
    {
	handleMouseMoved(-1, -1);
    }

    /**
     * Set the tool tip provider for display when the node map is
     * rendered.
     *
     * @param tipper the tool tip provider
     * @param x the last mouse x-position.
     * @param y the last mouse y-position.
     */
    public void setToolTipProvider (ToolTipProvider tipper, int x, int y)
    {
	_tipper = tipper;
	_tipx = x;
	_tipy = y;
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

    /** The current tool tip provider. */
    protected ToolTipProvider _tipper;

    /** The mouse position for calculating tool tip display position. */
    protected int _tipx, _tipy;

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
