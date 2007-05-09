//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util {

import flash.geom.Point;
import flash.geom.Matrix;

/**
 * Merely a typed container for two Points.
 */
public class Line implements Equalable
{
    public static const INTERSECTION_NORTH :int = 1;
    public static const INTERSECTION_SOUTH :int = 2;
    public static const DOES_NOT_INTERSECT :int = 3;

    public var start :Point;
    public var stop :Point;

    public function Line (start :Point, stop :Point)
    {
        this.start = start;
        this.stop = stop;
    }

    /**
     * Get the length of this line.
     */
    public function getLength () :Number
    {
        return Point.distance(start, stop);
    }

    public function isIntersected (line :Line) :Boolean 
    {
        return getIntersectionType(line) != DOES_NOT_INTERSECT;
    }

    /**
     * Return the point at which the other line intersects us.
     */
    public function getIntersectionPoint (line :Line) :Point
    {
        return getIntersection(line, true) as Point;
    }

    /**
     * Tests if the given line intersects this line. This method rotates both lines so that the 
     * start point of this line is on the left, at (0, 0).  If the lines do intersect, it then 
     * returns INTERSECTION_NORTH if the end point of <code>line</code> is north of this line, 
     * INTERSECTION_SOUTH otherwise.
     * 
     * Intersections are inclusive.  If one or both points lands on this line, interects will not
     * return DOES_NOT_INTERSECT.
     */
    public function getIntersectionType (line :Line) :int 
    {
        return getIntersection(line, false) as int;
    }

    // from interface Equalable
    public function equals (o :Object) :Boolean
    {
        var other :Line = o as Line; // or null if not a line
        if (other == null) {
            return false;
        }

        // both end points must be the same, in the same order
        return start.equals(other.start) && stop.equals(other.stop);
    }

    /**
     * Internal method that calculates whether the other line intersects
     * and returns either the intersected point or merely the intersection
     * type.
     */
    protected function getIntersection (line :Line, returnPoint :Boolean) :*
    {
        // rotate so that this line is horizontal, with the start on the left, at (0, 0)
        var trans :Matrix = new Matrix();
        trans.translate(-start.x, -start.y);
        trans.rotate(-Math.atan2(stop.y - start.y, stop.x - start.x));
        var thisLineStop :Point = trans.transformPoint(stop);
        var thatLineStart :Point = trans.transformPoint(line.start);
        var thatLineStop :Point = trans.transformPoint(line.stop);
        var interp :Point;
        var type :int;

        if (thatLineStart.y >= 0 && thatLineStop.y <= 0) {
            interp = Point.interpolate(thatLineStart, thatLineStop, thatLineStop.y / 
                (thatLineStop.y + (-thatLineStart.y)));
            type = INTERSECTION_NORTH;

        } else if (thatLineStart.y <= 0 && thatLineStop.y >= 0) {
            interp = Point.interpolate(thatLineStop, thatLineStart, thatLineStart.y / 
                (thatLineStart.y + (-thatLineStop.y)));
            type = INTERSECTION_SOUTH;
        }

        // see if we have a potential hit...
        if (interp != null && interp.x >= 0 && interp.x <= thisLineStop.x) {
            if (returnPoint) {
                // transform the intersection point back into "real" coordinates
                trans.invert();
                return trans.transformPoint(interp);

            } else {
                return type
            }
        }

        return returnPoint ? null : DOES_NOT_INTERSECT;
    }
}
}
