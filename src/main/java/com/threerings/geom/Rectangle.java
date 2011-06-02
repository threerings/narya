package com.threerings.geom;

import com.samskivert.util.Logger;

/**
 * A very simple representation of the geometric shape, because sometimes we can't use awt.
 */
public class Rectangle
{
    /** The left-most x-coordinate. */
    public int x;

    /** The top-most y-coordinate. */
    public int y;

    /** The width of the rectangle. */
    public int width;

    /** The height of the rectangle. */
    public int height;

    public Rectangle ()
    {
        this(0, 0, 0, 0);
    }

    public Rectangle (int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle (Rectangle other)
    {
        x = other.x;
        y = other.y;
        width = other.width;
        height = other.height;
    }

    /**
     * Returns the area of intersection of ourselves and another rectangle.
     */
    public Rectangle intersect (Rectangle other)
    {
        int x1 = Math.max(x, other.x), y1 = Math.max(y, other.y),
            x2 = Math.min(x + width, other.x + other.width),
            y2 = Math.min(y + height, other.y + other.height);
        return new Rectangle(x1, y1, Math.max(0, x2 - x1), Math.max(0, y2 - y1));
    }

    /**
     * Returns whether the specified coordinates are within the rectangle.
     */
    public boolean contains (int x, int y)
    {
        return (x >= this.x && x <= (this.x + width) && y >= this.y && y <= (this.y + height));
    }

    /**
     * Returns whether this rectangle covers any area.
     */
    public boolean isEmpty ()
    {
        return width <= 0 || height <= 0;
    }

    /**
     * Sets our position and size.
     */
    public void set (int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString ()
    {
        return Logger.format(getClass().getName(), "x", x, "y", y, "w", width, "h", height);
    }
}
