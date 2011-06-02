package com.threerings.geom;

import com.samskivert.util.Logger;

/**
 * A very simple representation of the geometric concept, because sometimes we can't use awt.
 */
public class Point
{
    /** The x-coordinate. */
    public int x;

    /** The y-coordinate. */
    public int y;

    public Point ()
    {
        this(0, 0);
    }

    public Point (int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString ()
    {
        return Logger.format(getClass().getName(), "x", x, "y", y);
    }
}
