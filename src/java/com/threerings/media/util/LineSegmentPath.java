//
// $Id: LineSegmentPath.java,v 1.8 2001/09/05 00:40:17 shaper Exp $

package com.threerings.media.sprite;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.StringUtil;

/**
 * The <code>Path</code> class represents the path a sprite follows
 * while meandering about the screen.  There must be at least two
 * nodes in any worthwhile path.  The direction of the first node in
 * the path is meaningless since the sprite begins at that node and
 * will therefore never be heading towards it.
 */
public class Path
{
    /** The number of distinct directions. */
    public static final int NUM_DIRECTIONS = 8;

    /** Direction constants. */
    public static final int DIR_NONE = -1;
    public static final int DIR_SOUTHWEST = 0;
    public static final int DIR_WEST = 1;
    public static final int DIR_NORTHWEST = 2;
    public static final int DIR_NORTH = 3;
    public static final int DIR_NORTHEAST = 4;
    public static final int DIR_EAST = 5;
    public static final int DIR_SOUTHEAST = 6;
    public static final int DIR_SOUTH = 7;

    /** String translations for the direction constants. */
    public static String[] XLATE_DIRS = {
	"Southwest", "West", "Northwest", "North", "Northeast",
	"East", "Southeast", "South"
        };

    /**
     * Construct a <code>Path</code> object.
     */
    public Path ()
    {
        _nodes = new ArrayList();
    }

    /**
     * Construct a <code>Path</code> object with the specified
     * starting node coordinates.  An arbitrary direction will be
     * assigned to the starting node.
     *
     * @param x the starting node x-position.
     * @param y the starting node y-position.
     */
    public Path (int x, int y)
    {
        _nodes = new ArrayList();

        // add the starting node with an arbitrarily chosen direction
        // since direction is meaningless here
        addNode(x, y, DIR_NORTH);
    }

    /**
     * Add a node to the path with the specified destination point and
     * facing direction.
     *
     * @param x the x-position.
     * @param y the y-position.
     * @param dir the facing direction.
     */
    public void addNode (int x, int y, int dir)
    {
        _nodes.add(new PathNode(x, y, dir));
    }

    /**
     * Add a node to the path with the specified destination point.
     * An arbitrary direction will be assigned to the node.
     *
     * @param x the x-position.
     * @param y the y-position.
     */
    public void addNode (int x, int y)
    {
        _nodes.add(new PathNode(x, y, Path.DIR_NORTH));
    }

    /**
     * Return the requested node index in the path, or null if no such
     * index exists.
     *
     * @param idx the node index.
     *
     * @return the path node.
     */
    public PathNode getNode (int idx)
    {
        return (PathNode)_nodes.get(idx);
    }

    /**
     * Return an enumeration of the PathNode objects in this path. 
     */
    public Iterator elements ()
    {
        return _nodes.iterator();
    }

    /**
     * Return the number of nodes in the path.
     */
    public int size ()
    {
        return _nodes.size();
    }

    public String toString ()
    {
        return StringUtil.toString(_nodes.iterator());
    }

    /** The nodes that make up the path. */
    protected ArrayList _nodes;
}
