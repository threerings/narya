//
// $Id: PointSet.java,v 1.2 2004/08/27 02:20:33 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.puzzle.util;

import java.awt.Point;
import java.util.Iterator;

import com.threerings.puzzle.Log;

/**
 * The point set class provides an efficient implementation of a set
 * containing two-dimensional point values as <code>(x, y)</code>.
 */
public class PointSet
{
    /**
     * Creates a point set that can contain points within the given range
     * of values.
     *
     * @param rangeX the maximum x-axis range.
     * @param rangeY the maximum y-axis range.
     */
    public PointSet (int rangeX, int rangeY)
    {
	_rangeX = rangeX;
	_rangeY = rangeY;
	_points = new boolean[rangeX][rangeY];
    }

    /**
     * Adds a point to the set and returns whether the point was already
     * present in the set.
     *
     * @param x the point x-coordinate.
     * @param y the point y-coordinate.
     *
     * @return true if the point was already present, false if not.
     */
    public boolean add (int x, int y)
    {
	boolean present = _points[x][y];
	_points[x][y] = true;
	if (!present) {
	    _count++;
	}
	return present;
    }

    /**
     * Adds all points in the given set to this set.
     *
     * @param set the set containing points to add.
     */
    public void addAll (PointSet set)
    {
	Iterator iter = set.iterator();
	Point pt;
	while ((pt = (Point)iter.next()) != null) {
	    add(pt.x, pt.y);
	}
    }

    /**
     * Clears all points from this set.
     */
    public void clear ()
    {
	if (_count == 0) {
	    // no need to clear anything
	    return;
	}

	for (int xx = 0; xx < _rangeX; xx++) {
	    for (int yy = 0; yy < _rangeY; yy++) {
		_points[xx][yy] = false;
	    }
	}
	_count = 0;
    }

    /**
     * Returns whether this set contains the given point.
     *
     * @param x the point x-coordinate.
     * @param y the point y-coordinate.
     *
     * @return true if the set contains the point, false if not.
     */
    public boolean contains (int x, int y)
    {
	return (_points[x][y]);
    }

    /**
     * Returns whether this set is empty.
     *
     * @return true if the set is empty, false if not.
     */
    public boolean isEmpty ()
    {
	return (_count == 0);
    }

    /**
     * Returns an iterator that iterates over the points in this set,
     * returning them as {@link Point} objects.  Note that the iterator
     * uses a single point object internally, and so callers should create
     * their own copy of the point if they plan to do something fancy with
     * it.
     *
     * @return the iterator over the set's points.
     */
    public Iterator iterator ()
    {
	return new PointIterator();
    }

    /**
     * Removes the given point from the set and returns whether the point
     * was present in the set.
     *
     * @param x the point x-coordinate.
     * @param y the point y-coordinate.
     *
     * @return true if the point was present, false if not.
     */
    public boolean remove (int x, int y)
    {
	boolean present = _points[x][y];
	_points[x][y] = false;
	if (present) {
	    _count--;
	}
	return present;
    }

    /**
     * Returns the number of points in the set.
     *
     * @return the number of points.
     */
    public int size ()
    {
	return _count;
    }

    /**
     * Returns a string representation of the point set.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[");
	Iterator iter = iterator();
	Point val;
	while ((val = (Point)iter.next()) != null) {
	    buf.append("(").append(val.x);
	    buf.append(",").append(val.y);
	    buf.append(")");

	    if (iter.hasNext()) {
		buf.append(", ");
	    }
	}
	return buf.append("]").toString();
    }

    protected class PointIterator implements Iterator
    {
	public boolean hasNext ()
	{
	    return (_curCount < _count);
	}

	public Object next ()
	{
	    if (_curCount == _count) {
		return null;
	    }

	    while (!_points[_curX][_curY]) {
		advance();
	    }

	    _curCount++;
	    _point.setLocation(_curX, _curY);

	    if (_curCount < _count) {
		advance();
	    }

	    return _point;
	}

	public void remove ()
	{
	    throw new UnsupportedOperationException();
	}

	protected void advance ()
	{
	    if ((++_curX) >= _rangeX) {
		_curX = 0;
		_curY++;
	    }

	    if (_curY >= _rangeY) {
		Log.warning("Advanced past point range.");
		_curY = 0;
	    }
	}

	protected int _curCount = 0;
	protected int _curX = 0, _curY = 0;
	protected Point _point = new Point();
    }

    /** The dimensions of the point array. */
    protected int _rangeX, _rangeY;

    /** The points in the set. */
    protected boolean _points[][];

    /** The number of points in the set. */
    protected int _count;
}
