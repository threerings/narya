//
// $Id: DirectionalLayoutManager.java,v 1.4 2001/12/17 03:34:04 mdb Exp $

package com.threerings.nodemap.direction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.threerings.util.DirectionCodes;

import com.threerings.nodemap.Node;
import com.threerings.nodemap.LayoutManager;

/**
 * The directional layout manager lays nodes out according to the eight
 * cardinal directions as specified in the {@link DirectionCodes} class.
 * The nodes must be fully connected in both directions with {@link
 * DirectionalEdge} edges.
 */
public class DirectionalLayoutManager
    implements LayoutManager, DirectionCodes
{
    /**
     * Construct a directional layout manager that lays out nodes
     * connected by edges that are <code>edgelen</code> pixels long.
     *
     * @param edgelen the length of the edges in pixels.
     */
    public DirectionalLayoutManager (int edgelen)
    {
	_edgelen = edgelen;
	_closed = new HashMap();
    }

    public void layout (Node root, List nodes)
    {
	_closed.clear();

	// lay out all nodes
	layoutNode(root, 0, 0, root);
    }

    protected void layoutNode (Node root, int x, int y, Node n)
    {
	// consider each node only once for now
	if (_closed.containsKey(n)) {
	    return;
	}

	// set the node's location
	n.setLocation(x, y);

	// remember that we've placed this node
	_closed.put(n, null);

	Iterator iter = n.getEdges();
	while (iter.hasNext()) {
	    DirectionalEdge e = (DirectionalEdge)iter.next();

	    int tx = x, ty = y;
	    int wid = e.dst.getWidth(), hei = e.dst.getHeight();

	    // shift the node in the appropriate direction
	    switch (e.dir) {
	    case NORTH:
		tx -= (_edgelen + wid);
		ty -= (_edgelen + hei);
		break;

	    case NORTHEAST:
		ty -= (_edgelen + hei);
		break;

	    case EAST:
		tx += (_edgelen + wid);
		ty -= (_edgelen + hei);
		break;

	    case SOUTHEAST:
		tx += (_edgelen + wid);
		break;

	    case SOUTH:
		tx += (_edgelen + wid);
		ty += (_edgelen + hei);
		break;

	    case SOUTHWEST:
		ty += (_edgelen + hei);
		break;

	    case WEST:
		tx -= (_edgelen + wid);
		ty += (_edgelen + hei);
		break;

	    case NORTHWEST:
		tx -= (_edgelen + wid);
		break;
	    }

	    layoutNode(root, tx, ty, e.dst);
	}
    }

    /** The length of the edges between nodes in pixels. */
    protected int _edgelen;

    /** The set of nodes already positioned. */
    protected HashMap _closed;
}
