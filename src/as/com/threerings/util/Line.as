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

    public function isIntersected (line :Line) :Boolean 
    {
        return getIntersectionType(line) != DOES_NOT_INTERSECT;
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
        // rotate so that this line is horizontal, with the start on the left, at (0, 0)
        var trans :Matrix = new Matrix();
        trans.translate(-start.x, -start.y);
        trans.rotate(-Math.atan2(stop.y - start.y, stop.x - start.x));
        var thisLineStop :Point = trans.transformPoint(stop);
        var thatLineStart :Point = trans.transformPoint(line.start);
        var thatLineStop :Point = trans.transformPoint(line.stop);
        if (thatLineStart.y >= 0 && thatLineStop.y <= 0) {
            var interp :Point = Point.interpolate(thatLineStart, thatLineStop, thatLineStop.y / 
                (thatLineStop.y + (-thatLineStart.y)));
            if (interp.x >= 0 && interp.x <= thisLineStop.x) {
                return INTERSECTION_NORTH;
            } else {
                return DOES_NOT_INTERSECT;
            }
        } else if (thatLineStart.y <= 0 && thatLineStop.y >= 0) {
            interp = Point.interpolate(thatLineStop, thatLineStart, thatLineStart.y / 
                (thatLineStart.y + (-thatLineStop.y)));
            if (interp.x >= 0 && interp.x <= thisLineStop.x) {
                return INTERSECTION_SOUTH;
            } else {
                return DOES_NOT_INTERSECT;
            }
        } else {
            return DOES_NOT_INTERSECT;
        }
    }

    // from interface Equalable
    public function equals (o :Object) :Boolean
    {
        var other :Line = o as Line;
        if (other == null) {
            return false;
        }

        if (start == null || other.start == null) {
            if (start != other.start) {
                // not both null
                return false;
            }
        }
        if (stop == null || other.stop == null) {
            if (stop != other.stop) {
                // not both null
                return false;
            }
        }

        return (start == null || start.equals(other.start)) && 
               (stop == null || stop.equals(other.stop));
    }
}
}
