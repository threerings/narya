//
// $Id: LineSegmentPath.java,v 1.1 2001/08/02 00:42:02 shaper Exp $

package com.threerings.miso.sprite;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * The Path class represents the path a sprite follows while
 * meandering about the screen.  There must be at least two nodes in
 * any worthwhile path.
 */
public class Path
{
    /** The number of distinct directions. */
    public static final int NUM_DIRECTIONS = 8;

    /** Direction constants. */
    public static final int DIR_SOUTH = 0;
    public static final int DIR_SOUTHWEST = 1;
    public static final int DIR_WEST = 2;
    public static final int DIR_NORTHWEST = 3;
    public static final int DIR_NORTH = 4;
    public static final int DIR_NORTHEAST = 5;
    public static final int DIR_EAST = 6;
    public static final int DIR_SOUTHEAST = 7;

    /**
     * Construct a Path object.
     */
    public Path ()
    {
        _nodes = new ArrayList();
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
     * Return an enumeration of the PathNode objects in this path. 
     */
    public Enumeration elements ()
    {
        return new Enumerator(_nodes);
    }

    /**
     * Return the number of nodes in the path.
     */
    public int size ()
    {
        return _nodes.size();
    }

    /**
     * Internal class that provides enumeration functionality for the path.
     */
    class Enumerator implements Enumeration
    {
        public Enumerator (ArrayList nodes)
        {
            _nodes = nodes;
            _idx = 0;
        }

        public boolean hasMoreElements()
        {
            return (_idx < _nodes.size());
        }

        public Object nextElement ()
        {
            return _nodes.get(_idx++);
        }

        protected ArrayList _nodes;
        protected int _idx;
    }

    /** The nodes that make up the path. */
    protected ArrayList _nodes;
}
