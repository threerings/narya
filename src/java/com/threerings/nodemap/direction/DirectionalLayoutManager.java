//
// $Id: DirectionalLayoutManager.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap.direction;

import java.util.*;

import com.threerings.nodemap.*;

/**
 * The directional layout manager lays nodes out according to the
 * eight cardinal directions as specified in the {@link Directions}
 * class.  The nodes must be connected with {@link DirectionalEdge}
 * edges.
 */
public class DirectionalLayoutManager implements LayoutManager
{
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
	n.setPosition(x, y);

	// remember that we've placed this node
	_closed.put(n, null);

	Iterator iter = n.getEdges();
	while (iter.hasNext()) {
	    DirectionalEdge e = (DirectionalEdge)iter.next();

	    int tx = x, ty = y;
	    int wid = e.dst.getWidth(), hei = e.dst.getHeight();

	    // shift the node in the appropriate direction
	    switch (e.dir) {
	    case Directions.NORTH:
		tx -= (_edgelen + wid);
		ty -= (_edgelen + hei);
		break;

	    case Directions.NORTHEAST:
		ty -= (_edgelen + hei);
		break;

	    case Directions.EAST:
		tx += (_edgelen + wid);
		ty -= (_edgelen + hei);
		break;

	    case Directions.SOUTHEAST:
		tx += (_edgelen + wid);
		break;

	    case Directions.SOUTH:
		tx += (_edgelen + wid);
		ty += (_edgelen + hei);
		break;

	    case Directions.SOUTHWEST:
		ty += (_edgelen + hei);
		break;

	    case Directions.WEST:
		tx -= (_edgelen + wid);
		ty += (_edgelen + hei);
		break;

	    case Directions.NORTHWEST:
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
