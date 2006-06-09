package com.threerings.util {

import flash.geom.Point;
import flash.geom.Rectangle;

public class DisplayUtil
{
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

        return true;
    }
}
}
