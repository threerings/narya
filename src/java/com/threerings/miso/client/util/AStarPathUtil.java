//
// $Id: AStarPathUtil.java,v 1.12 2002/02/07 20:02:53 mdb Exp $

package com.threerings.miso.scene.util;

import java.awt.Point;
import java.util.*;

import com.threerings.media.util.MathUtil;

import com.threerings.miso.Log;
import com.threerings.miso.scene.Traverser;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileLayer;

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
     * Return a list of <code>Point</code> objects representing a path
     * from coordinates <code>(ax, by)</code> to
     * <code>(bx, by)</code>, inclusive, determined by performing an
     * A* search in the given array of tiles.  Assumes the starting
     * and destination nodes are traversable by the specified
     * traverser.
     *
     * @param tiles the tile array.
     * @param tilewid the tile array width.
     * @param tilehei the tile array height.
     * @param trav the traverser to follow the path.
     * @param ax the starting x-position in tile coordinates.
     * @param ay the starting y-position in tile coordinates.
     * @param bx the ending x-position in tile coordinates.
     * @param by the ending y-position in tile coordinates.
     *
     * @return the list of points in the path.
     */
    public static List getPath (
	BaseTileLayer tiles, int tilewid, int tilehei, Traverser trav,
	int ax, int ay, int bx, int by)
    {
	AStarInfo info = new AStarInfo(tiles, tilewid, tilehei, trav, bx, by);

	// set up the starting node
	AStarNode s = getNode(info, ax, ay);
	s.g = 0;
	s.h = getDistanceEstimate(ax, ay, bx, by);
	s.f = s.g + s.h;

	// push starting node on the open list
	info.open.add(s);

	// while there are more nodes on the open list
	while (info.open.size() > 0) {

	    // pop the best node so far from open
	    AStarNode n = (AStarNode)info.open.first();
	    info.open.remove(n);

	    // if node is a goal node
	    if (n.x == bx && n.y == by) {
		// construct and return the acceptable path
		return getNodePath(n);
	    }

	    // consider each successor of the node
	    considerStep(info, n, n.x - 1, n.y - 1, 14);
	    considerStep(info, n, n.x, n.y - 1, 10);
	    considerStep(info, n, n.x + 1, n.y - 1, 14);
	    considerStep(info, n, n.x - 1, n.y, 10);
	    considerStep(info, n, n.x + 1, n.y, 10);
	    considerStep(info, n, n.x - 1, n.y + 1, 14);
	    considerStep(info, n, n.x, n.y + 1, 10);
	    considerStep(info, n, n.x + 1, n.y + 1, 14);

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
	AStarInfo info, AStarNode n, int x, int y, int cost)
    {
        // skip node if it's outside the map bounds or otherwise impassable
        if (!isStepValid(info, n.x, n.y, x, y)) {
            return;
        }

	// calculate the new cost for this node
	int newg = n.g + cost;

	// retrieve the node corresponding to this location
	AStarNode np = getNode(info, x, y);

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
    protected static List getNodePath (AStarNode n)
    {
	AStarNode cur = n;
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
     * Returns whether moving from the given source to destination
     * coordinates is a valid move.
     */
    protected static boolean isStepValid (
        AStarInfo info, int sx, int sy, int dx, int dy)
    {
        // not traversable if the destination itself fails test
	if (!isTraversable(info, dx, dy)) {
            return false;
        }

        // if the step is diagonal, make sure the corners don't impede
        // our progress
        if (dx == sx - 1 && dy == sy - 1) {
            return isTraversable(info, sx - 1, sy, sx, sy - 1);

        } else if (dx == sx + 1 && dy == sy - 1) {
            return isTraversable(info, sx, sy - 1, sx + 1, sy);

        } else if (dx == sx - 1 && dy == sy + 1) {
            return isTraversable(info, sx - 1, sy, sx, sy + 1);

        } else if (dx == sx + 1 && dy == sy + 1) {
            return isTraversable(info, sx + 1, sy, sx, sy + 1);
        }

        // non-diagonals are always traversable
        return true;
    }

    /**
     * Returns whether the given coordinate is valid and traversable.
     */
    protected static boolean isTraversable (AStarInfo info, int x, int y)
    {
        return (isCoordinateValid(info, x, y) &&
                info.trav.canTraverse(info.tiles.getTile(x, y)));
    }

    /**
     * Returns whether both of the given coordinates are valid and
     * traversable.
     */
    protected static boolean isTraversable (
        AStarInfo info, int x1, int y1, int x2, int y2)
    {
        return (isTraversable(info, x1, y1) &&
                isTraversable(info, x2, y2));
    }

    /**
     * Returns whether the given coordinate is valid based on the
     * dimensions of the map being traversed.
     */
    protected static boolean isCoordinateValid (AStarInfo info, int x, int y)
    {
	return (x >= 0 && y >= 0 && x < info.tilewid && y < info.tilehei);
    }

    /**
     * Return the <code>AStarNode</code> object corresponding to the
     * specified tile coordinate.  Creates the node and saves it in
     * the node array if this is its first reference.
     */
    protected static AStarNode getNode (AStarInfo info, int x, int y)
    {
	AStarNode n = info.nodes[x][y];
	return (n == null) ? (info.nodes[x][y] = new AStarNode(x, y)) : n;
    }

    /**
     * Return a heuristic estimate of the cost to get from <code>(ax,
     * ay)</code> to <code>(bx, by)</code>.
     */
    protected static int getDistanceEstimate (int ax, int ay, int bx, int by)
    {
        // we're doing all of our cost calculations based on geometric
        // distance times ten
        int xsq = 10 * (bx - ax); xsq = xsq * xsq;
        int ysq = 10 * (by - ay); ysq = ysq * ysq;
        return (int)Math.sqrt(xsq + ysq);
    }
}

/**
 * A holding class to contain the wealth of information referenced
 * while performing an A* search for a path through a tile array.
 */
class AStarInfo
{
    /** The tile layer being traversed. */
    public BaseTileLayer tiles;

    /** The tile array dimensions. */
    public int tilewid, tilehei;

    /** The traverser moving along the path. */
    public Traverser trav;

    /** The array of A*-specific node info to match the tile array. */
    public AStarNode nodes[][];

    /** The set of open nodes being searched. */
    public SortedSet open;

    /** The set of closed nodes being searched. */
    public ArrayList closed;

    /** The destination coordinates in the tile array. */
    public int destx, desty;

    public AStarInfo (
	BaseTileLayer tiles, int tilewid, int tilehei, Traverser trav,
	int destx, int desty)
    {
	// save off references
	this.tiles = tiles;
	this.tilewid = tilewid;
	this.tilehei = tilehei;
	this.trav = trav;
	this.destx = destx;
	this.desty = desty;

	// construct the node array
	nodes = new AStarNode[tilewid][tilehei];

	// construct the open and closed lists
	open = new TreeSet();
	closed = new ArrayList();
    }
}

/**
 * A class that represents a single traversable node in the tile array
 * along with its current A*-specific search information.
 */
class AStarNode implements Comparable
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
    public AStarNode parent;

    /** The node's monotonically-increasing unique identifier. */
    public int id;

    public AStarNode (int x, int y)
    {
	this.x = x;
	this.y = y;
	id = _nextid++;
    }

    public int compareTo (Object o)
    {
	int bf = ((AStarNode)o).f;

	// since the set contract is fulfilled using the equality results
	// returned here, and we'd like to allow multiple nodes with
	// equivalent scores in our set, we explicitly define object
	// equivalence as the result of object.equals(), else we use the
	// unique node id since it will return a consistent ordering for
	// the objects.
  	if (f == bf) {
	    return (this == o) ? 0 : (id - ((AStarNode)o).id);
  	}

	return f - bf;
    }

    /** The next unique node id. */
    protected static int _nextid = 0;
}
