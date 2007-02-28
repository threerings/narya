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

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

import flash.geom.Point;
import flash.geom.Rectangle;

import mx.core.IChildList;
import mx.core.IRawChildrenContainer;

public class DisplayUtil
{
    /**
     * Call the specified function for the display object and all descendants.
     *
     * This is nearly exactly like mx.utils.DisplayUtil.walkDisplayObjects,
     * except this method copes with security errors when examining a child.
     */
    public static function applyToHierarchy (
        disp :DisplayObject, callbackFunction :Function) :void
    {
        callbackFunction(disp);

        if (disp is DisplayObjectContainer) {
            // a little type-unsafety so that we don't have to write two blocks
            var o :Object = (disp is IRawChildrenContainer) ?
                IRawChildrenContainer(disp).rawChildren : disp;
            var nn :int = int(o.numChildren);
            for (var ii :int = 0; ii < nn; ii++) {
                try {
                    disp = DisplayObject(o.getChildAt(ii));
                } catch (err :SecurityError) {
                    continue;
                }
                // and then we apply outside of the try/catch block so that
                // we don't hide errors thrown by the callbackFunction.
                applyToHierarchy(disp, callbackFunction);
            }
        }
    }

    /**
     * Returns the most reasonable position for the specified rectangle to
     * be placed at so as to maximize its containment by the specified
     * bounding rectangle while still placing it as near its original
     * coordinates as possible.
     *
     * @param rect the rectangle to be positioned.
     * @param bounds the containing rectangle.
     */
    public static function fitRectInRect (
            rect :Rectangle , bounds :Rectangle) :Point
    {
        // Guarantee that the right and bottom edges will be contained
        // and do our best for the top and left edges.
        var br :Point = bounds.bottomRight;
        return new Point(
            Math.min(br.x - rect.width, Math.max(rect.x, bounds.x)),
            Math.min(br.y - rect.height, Math.max(rect.y, bounds.y)));
    }

    /**
     * Position the specified rectangle within the bounds, avoiding
     * any of the Rectangles in the avoid array, which may be destructively
     * modified.
     *
     * @return true if the rectangle was successfully placed, given the
     * constraints, or false if the positioning failed (the rectangle will
     * be left at its original location.
     */
    public static function positionRect (
            r :Rectangle, bounds :Rectangle, avoid :Array) :Boolean
    {
        var origPos :Point = r.topLeft;
        var pointSorter :Function = createPointSorter(origPos);
        var possibles :Array = new Array();
        // start things off with the passed-in point (adjusted to
        // be inside the bounds, if needed)
        possibles.push(fitRectInRect(r, bounds));

        // keep track of area that doesn't generate new possibles
        var dead :Array = new Array();

        // Note: labeled breaks and continues are supposed to be legal,
        // but they throw wacky runtime exceptions for me. So instead
        // I'm throwing a boolean and using that to continue the while
        /* CHECKPOSSIBLES: */ while (possibles.length > 0) {
            try {
                var p :Point = (possibles.shift() as Point);
                r.x = p.x;
                r.y = p.y;

                // make sure the rectangle is in the view
                if (!bounds.containsRect(r)) {
                    continue;
                }

                // and not over a dead area
                for each (var deadRect :Rectangle in dead) {
                    if (deadRect.intersects(r)) {
                        //continue CHECKPOSSIBLES;
                        throw true; // continue outer loop
                    }
                }

                // see if it hits any rects we're trying to avoid
                for (var ii :int = 0; ii < avoid.length; ii++) {
                    var avoidRect :Rectangle = (avoid[ii] as Rectangle);
                    if (avoidRect.intersects(r)) {
                        // remove it from the avoid list
                        avoid.splice(ii, 1);
                        // but add it to the dead list
                        dead.push(avoidRect);

                        // add 4 new possible points, each pushed in
                        // one direction
                        possibles.push(
                            new Point(avoidRect.x - r.width, r.y),
                            new Point(r.x, avoidRect.y - r.height),
                            new Point(avoidRect.x + avoidRect.width, r.y),
                            new Point(r.x, avoidRect.y + avoidRect.height));

                        // re-sort the list
                        possibles.sort(pointSorter);
                        //continue CHECKPOSSIBLES;
                        throw true; // continue outer loop
                    }
                }

                // hey! if we got here, then it worked!
                return true;

            } catch (continueWhile :Boolean) {
                // simply catch the boolean and use it to continue inner loops
            }
        }

        // we never found a match, move the rectangle back
        r.x = origPos.x;
        r.y = origPos.y;
        return false;
    }

    /**
     * Create a sort Function that can be used to compare Points in an
     * Array according to their distance from the specified Point.
     *
     * Note: The function will always sort according to distance from the
     * passed-in point, even if that point's coordinates change after
     * the function is created.
     */
    public static function createPointSorter (origin :Point) :Function
    {
        return function (p1 :Point, p2 :Point) :Number {
            var dist1 :Number = Point.distance(origin, p1);
            var dist2 :Number = Point.distance(origin, p2);

            return (dist1 > dist2) ? 1 : ((dist1 < dist2) ? -1 : 0); // signum
        };
    }
}
}
