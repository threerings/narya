package com.threerings.util {

import flash.geom.Point;

/**
 * Merely a typed container for two Points.
 */
public class Line
{
    public var start :Point;
    public var stop :Point;

    public function Line (start :Point, stop :Point)
    {
        this.start = start;
        this.stop = stop;
    }
}
}
