//
// $Id: AStarPathUtil.java,v 1.1 2003/04/17 19:21:16 mdb Exp $

package com.threerings.miso.util;

import java.awt.Point;
import java.util.*;

import com.samskivert.util.HashIntMap;

import com.threerings.media.util.MathUtil;

import com.threerings.miso.Log;
import com.threerings.miso.tile.BaseTile;

/**
 * The <code>AStarPathUtil</code> class provides a facility for
 * finding a reasonable path between two points in a scene using the
 * A* search algorithm.
 *
 * <p> See the path-finding article on
 * <a href="http://www.gamasutra.com/features/19990212/sm_01.htm">
 * Gamasutra</a> for more detailed information.
 */
public class AStarPathUtil
{
    /**
     * Provides traversibility information when computing paths.
     */
    public static interface TraversalPred
    {
        /**
         * Requests to know if the specified traverser (which was provided
         * in the call to {@link #getPath}) can traverse the specified
         * tile coordinate.
         */
        public boolean canTraverse (Object traverser, int x, int y);
    }

    /**
     * Return a list of <code>Point</code> objects representing a path
     * from coordinates <code>(ax, by)</code> to <code>(bx, by)</code>,
     * inclusive, determined by performing an A* search in the given
     * scene's base tile layer. Assumes the starting and destination nodes
     * are traversable by the specified traverser.
     *
     * @param tpred lets us know what tiles are traversible.
     * @param trav the traverser to follow the path.
     * @param longest the longest allowable path in tile traversals.
     * @param ax the starting x-position in tile coordinates.
     * @param ay the starting y-position in tile coordinates.
     * @param bx the ending x-position in tile coordinates.
     * @param by the ending y-position in tile coordinates.
     *
     * @return the list of points in the path.
     */
    public static List getPath (TraversalPred tpred, Object trav,
                                int longest, int ax, int ay, int bx, int by)
    {
	Info info = new Info(tpred, trav, longest, bx, by);

	// set up the starting node
	Node s = info.getNode(ax, ay);
	s.g = 0;
	s.h = getDistanceEstimate(ax, ay, bx, by);
	s.f = s.g + s.h;

	// push starting node on the open list
	info.open.add(s);

	// while there are more nodes on the open list
	while (info.open.size() > 0) {

	    // pop the best node so far from open
	    Node n = (Node)info.open.first();
	    info.open.remove(n);

	    // if node is a goal node
	    if (n.x == bx && n.y == by) {
		// construct and return the acceptable path
		return getNodePath(n);
	    }

	    // consider each successor of the node
	    considerStep(info, n, n.x - 1, n.y - 1, DIAGONAL_COST);
	    considerStep(info, n, n.x, n.y - 1, ADJACENT_COST);
	    considerStep(info, n, n.x + 1, n.y - 1, DIAGONAL_COST);
	    considerStep(info, n, n.x - 1, n.y, ADJACENT_COST);
	    considerStep(info, n, n.x + 1, n.y, ADJACENT_COST);
	    considerStep(info, n, n.x - 1, n.y + 1, DIAGONAL_COST);
	    considerStep(info, n, n.x, n.y + 1, ADJACENT_COST);
	    considerStep(info, n, n.x + 1, n.y + 1, DIAGONAL_COST);

	    // push the node on the closed list
	    info.closed.add(n);
	}

	// no path found
	return null;
    }

    /**
     * Consider the step <code>(n.x, n.y)</code> to <code>(x, y)</code>
     * for possible inclusion in the path.
     *
     * @param info the info object.
     * @param n the originating node for the step.
     * @param x the x-coordinate for the destination step.
     * @param y the y-coordinate for the destination step.
     */
    protected static void considerStep (
        Info info, Node n, int x, int y, int cost)
    {
        // skip node if it's outside the map bounds or otherwise impassable
        if (!info.isStepValid(n.x, n.y, x, y)) {
            return;
        }

	// calculate the new cost for this node
	int newg = n.g + cost;

        // make sure the cost is reasonable
        if (newg > info.maxcost) {
//            Log.info("Rejected costly step.");
            return;
        }

	// retrieve the node corresponding to this location
	Node np = info.getNode(x, y);

	// skip if it's already in the open or closed list or if its
	// actual cost is less than the just-calculated cost
	if ((info.open.contains(np) || info.closed.contains(np)) &&
	    np.g <= newg) {
	    return;
	}

	// remove the node from the open list since we're about to
	// modify its score which determines its placement in the list
	info.open.remove(np);

	// update the node's information
	np.parent = n;
	np.g = newg;
	np.h = getDistanceEstimate(np.x, np.y, info.destx, info.desty);
	np.f = np.g + np.h;

	// remove it from the closed list if it's present
	info.closed.remove(np);

	// add it to the open list for further consideration
	info.open.add(np);
    }

    /**
     * Return a list of <code>Point</code> objects detailing the path
     * from the first node (the given node's ultimate parent) to the
     * ending node (the given node itself.)
     *
     * @param n the ending node in the path.
     *
     * @return the list detailing the path.
     */
    protected static List getNodePath (Node n)
    {
	Node cur = n;
	ArrayList path = new ArrayList();

	while (cur != null) {
	    // add to the head of the list since we're traversing from
	    // the end to the beginning
	    path.add(0, new Point(cur.x, cur.y));

	    // advance to the next node in the path
	    cur = cur.parent;
	}

	return path;
    }

    /**
     * Return a heuristic estimate of the cost to get from <code>(ax,
     * ay)</code> to <code>(bx, by)</code>.
     */
    protected static int getDistanceEstimate (int ax, int ay, int bx, int by)
    {
        // we're doing all of our cost calculations based on geometric
        // distance times ten
        int xsq = bx - ax;
        int ysq = by - ay;
        return (int) (ADJACENT_COST * Math.sqrt(xsq * xsq + ysq * ysq));
    }

    /**
     * A holding class to contain the wealth of information referenced
     * while performing an A* search for a path through a tile array.
     */
    protected static class Info
    {
        /** Knows whether or not tiles are traversable. */
        public TraversalPred tpred;

        /** The tile array dimensions. */
        public int tilewid, tilehei;

        /** The traverser moving along the path. */
        public Object trav;

        /** The set of open nodes being searched. */
        public SortedSet open;

        /** The set of closed nodes being searched. */
        public ArrayList closed;

        /** The destination coordinates in the tile array. */
        public int destx, desty;

        /** The maximum cost of any path that we'll consider. */
        public int maxcost;

        public Info (TraversalPred tpred, Object trav,
                     int longest, int destx, int desty)
        {
            // save off references
            this.tpred = tpred;
            this.trav = trav;
            this.destx = destx;
            this.desty = desty;

            // compute our maximum path cost
            this.maxcost = longest * ADJACENT_COST;

            // construct the open and closed lists
            open = new TreeSet();
            closed = new ArrayList();
        }

        /**
         * Returns whether moving from the given source to destination
         * coordinates is a valid move.
         */
        protected boolean isStepValid (int sx, int sy, int dx, int dy)
        {
            // not traversable if the destination itself fails test
            if (!isTraversable(dx, dy)) {
                return false;
            }

            // if the step is diagonal, make sure the corners don't impede
            // our progress
            if ((Math.abs(dx - sx) == 1) && (Math.abs(dy - sy) == 1)) {
                return isTraversable(dx, sy) && isTraversable(sx, dy);
            }

            // non-diagonals are always traversable
            return true;
        }

        /**
         * Returns whether the given coordinate is valid and traversable.
         */
        protected boolean isTraversable (int x, int y)
        {
            return tpred.canTraverse(trav, x, y);
        }

        /**
         * Get or create the node for the specified point.
         */
        public Node getNode (int x, int y)
        {
            // note: this _could_ break for unusual values of x and y.
            // perhaps use a IntTuple as a key? Bleah.
            int key = (x << 16) | (y & 0xffff);
            Node node = (Node) _nodes.get(key);
            if (node == null) {
                node = new Node(x, y);
                _nodes.put(key, node);
            }
            return node;
        }

        /** The nodes being considered in the path. */
        protected HashIntMap _nodes = new HashIntMap();
    }

    /**
     * A class that represents a single traversable node in the tile array
     * along with its current A*-specific search information.
     */
    protected static class Node implements Comparable
    {
        /** The node coordinates. */
        public int x, y;

        /** The actual cheapest cost of arriving here from the start. */
        public int g;

        /** The heuristic estimate of the cost to the goal from here. */
        public int h;

        /** The score assigned to this node. */
        public int f;

        /** The node from which we reached this node. */
        public Node parent;

        /** The node's monotonically-increasing unique identifier. */
        public int id;

        public Node (int x, int y)
        {
            this.x = x;
            this.y = y;
            id = _nextid++;
        }

        public int compareTo (Object o)
        {
            int bf = ((Node)o).f;

            // since the set contract is fulfilled using the equality results
            // returned here, and we'd like to allow multiple nodes with
            // equivalent scores in our set, we explicitly define object
            // equivalence as the result of object.equals(), else we use the
            // unique node id since it will return a consistent ordering for
            // the objects.
            if (f == bf) {
                return (this == o) ? 0 : (id - ((Node)o).id);
            }

            return f - bf;
        }

        /** The next unique node id. */
        protected static int _nextid = 0;
    }

    /** The standard cost to move between nodes. */
    public static final int ADJACENT_COST = 10;

    /** The cost to move diagonally. */
    public static final int DIAGONAL_COST = (int)Math.sqrt(
        (ADJACENT_COST * ADJACENT_COST) * 2);
}
