//
// $Id: LineSegmentPath.java,v 1.10 2001/09/17 23:55:45 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics2D;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.StringUtil;
import com.threerings.media.util.MathUtil;

/**
 * The <code>LineSegmentPath</code> class is used to cause a sprite to
 * follow a path that is made up of a sequence of line segments. There
 * must be at least two nodes in any worthwhile path. The direction of the
 * first node in the path is meaningless since the sprite begins at that
 * node and will therefore never be heading towards it.
 */
public class LineSegmentPath implements Path
{
    /**
     * Construct a <code>LineSegmentPath</code> object.
     */
    public LineSegmentPath ()
    {
        _nodes = new ArrayList();
    }

    /**
     * Construct a <code>LineSegmentPath</code> object with the specified
     * starting node coordinates.  An arbitrary direction will be assigned
     * to the starting node.
     *
     * @param x the starting node x-position.
     * @param y the starting node y-position.
     */
    public LineSegmentPath (int x, int y)
    {
        _nodes = new ArrayList();

        // add the starting node with an arbitrarily chosen direction
        // since direction is meaningless here
        addNode(x, y, Sprite.DIR_NORTH);
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
     * Add a node to the path with the specified destination point. An
     * arbitrary direction will be assigned to the node.
     *
     * @param x the x-position.
     * @param y the y-position.
     */
    public void addNode (int x, int y)
    {
        _nodes.add(new PathNode(x, y, Sprite.DIR_NORTH));
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
     * Return the number of nodes in the path.
     */
    public int size ()
    {
        return _nodes.size();
    }

    /**
     * Sets the velocity of this sprite in pixels per millisecond. The
     * velocity is measured as pixels traversed along the path that
     * the sprite is traveling rather than in the x or y directions
     * individually.  Note that the sprite velocity should not be
     * changed while a path is being traversed; doing so may result in
     * the sprite position changing unexpectedly.
     *
     * @param velocity the sprite velocity in pixels per millisecond.
     */
    public void setVelocity (float velocity)
    {
        _vel = velocity;
    }

    // documentation inherited
    public void init (Sprite sprite, long timestamp)
    {
        // if we have only one node then let the sprite know that we're
        // done straight away
        if (size() < 2) {
            // move the sprite to the location specified by the first node
            // (assuming we have a first node)
            if (size() == 1) {
                PathNode node = (PathNode)_nodes.get(0);
                sprite.setLocation(node.loc.x, node.loc.y);
            }
            // and let the sprite know that we're done
	    sprite.pathCompleted();
	    return;
	}

        // and an enumeration of the path nodes
        _niter = _nodes.iterator();

	// pretend like we were previously heading to our starting position
        _dest = (PathNode)_niter.next();

        // begin traversing the path
        headToNextNode(sprite, timestamp, timestamp);
    }

    // documentation inherited
    public boolean updatePosition (Sprite sprite, long timestamp)
    {
        // figure out how far along this segment we should be
        long msecs = timestamp - _nodestamp;
        float travpix = msecs * _vel;
        float pctdone = travpix / _seglength;

        // if we've moved beyond the end of the path, we need to adjust
        // the timestamp to determine how much time we used getting to the
        // end of this node, then move to the next one
        if (pctdone >= 1.0) {
            long used = (long)(_seglength / _vel);
            return headToNextNode(sprite, _nodestamp + used, timestamp);
        }

        // otherwise we position the sprite along the path
        int ox = sprite.getX();
        int oy = sprite.getY();
        int nx = _src.loc.x + (int)((_dest.loc.x - _src.loc.x) * pctdone);
        int ny = _src.loc.y + (int)((_dest.loc.y - _src.loc.y) * pctdone);

        // only update the sprite's location if it actually moved
        if (ox != nx || oy != ny) {
            sprite.setLocation(nx, ny);
            return true;
        }        

        return false;
    }

    protected boolean headToNextNode (Sprite sprite, long startstamp, long now)
    {
        // check to see if we've completed our path
        if (!_niter.hasNext()) {
            // move the sprite to the location of our last destination
            sprite.setLocation(_dest.loc.x, _dest.loc.y);
            sprite.pathCompleted();
            return true;
        }

        // our previous destination is now our source
        _src = _dest;

        // pop the next node off the path
        _dest = (PathNode)_niter.next();

        // adjust the sprite's orientation
        sprite.setOrientation(_dest.dir);

        // make a note of when we started traversing this node
        _nodestamp = startstamp;

        // figure out the distance from source to destination
        _seglength = MathUtil.distance(_src.loc.x, _src.loc.y,
                                       _dest.loc.x, _dest.loc.y);

        // if we're already there (the segment length is zero), we skip to
        // the next segment
        if (_seglength == 0) {
            return headToNextNode(sprite, startstamp, now);
        }

        // now update the sprite's position based on our progress thus far
        return updatePosition(sprite, now);
    }

    public void paint (Graphics2D gfx)
    {
	gfx.setColor(Color.red);
	Point prev = null;
	int size = size();
	for (int ii = 0; ii < size; ii++) {
	    PathNode n = (PathNode)getNode(ii);
	    if (prev != null) {
                gfx.drawLine(prev.x, prev.y, n.loc.x, n.loc.y);
            }
	    prev = n.loc;
	}
    }

    public String toString ()
    {
        return StringUtil.toString(_nodes.iterator());
    }

    /** The nodes that make up the path. */
    protected ArrayList _nodes;

    /** We use this when moving along this path. */
    protected Iterator _niter;

    /** When moving, the sprite's source path node. */
    protected PathNode _src;

    /** When moving, the sprite's destination path node. */
    protected PathNode _dest;

    /** The time at which we started traversing the current node. */
    protected long _nodestamp;

    /** The length in pixels of the current path segment. */
    protected float _seglength;

    /** The path velocity in pixels per millisecond. */
    protected float _vel = DEFAULT_VELOCITY;

    /** When moving, the sprite position including fractional pixels. */ 
    protected float _movex, _movey;

    /** When moving, the distance to move on each axis per tick. */
    protected float _incx, _incy;

    /** The distance to move on the straight path line per tick. */
    protected float _fracx, _fracy;

    /** Default sprite velocity. */
    protected static final float DEFAULT_VELOCITY = 200f/1000f;
}
