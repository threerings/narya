//
// $Id: MathUtil.java,v 1.10 2004/02/25 14:43:17 mdb Exp $

package com.threerings.media.util;

import java.awt.Point;

/**
 * Provides miscellaneous useful utility routines for mathematical
 * calculations.
 */
public class MathUtil
{
    /**
     * Bounds the supplied value within the specified range.
     *
     * @return low if value < low, high if value > high and value
     * otherwise.
     */
    public static int bound (int low, int value, int high)
    {
        return Math.min(high, Math.max(low, value));
    }

    /**
     * Return the distance between the given points.
     */
    public static float distance (int x0, int y0, int x1, int y1)
    {
	return (float)Math.sqrt(((x1 - x0) * (x1 - x0)) +
				((y1 - y0) * (y1 - y0)));
    }

    /**
     * Return the distance between the given points.
     */
    public static float distance (Point source, Point dest)
    {
        return MathUtil.distance(source.x, source.y, dest.x, dest.y);
    }

    /**
     * Return a string representation of the given line.
     */
    public static String lineToString (int x0, int y0, int x1, int y1)
    {
	return "(" + x0 + ", " + y0 + ") -> (" + x1 + ", " + y1 + ")";
    }

    /**
     * Return a string representation of the given line.
     */
    public static String lineToString (Point p1, Point p2)
    {
	return lineToString(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Returns the approximate circumference of the ellipse defined by the
     * specified minor and major axes. The formula used (due to Ramanujan,
     * via a paper of his entitled "Modular Equations and Approximations
     * to Pi"), is <code>Pi(3a + 3b - sqrt[(a+3b)(b+3a)])</code>.
     */
    public static double ellipseCircum (double a, double b)
    {
        return Math.PI * (3*a + 3*b - Math.sqrt((a + 3*b) * (b + 3*a)));
    }

    /**
     * Returns positive 1 if the sign of the argument is positive, or -1
     * if the sign of the argument is negative.
     */
    public static int sign (int value)
    {
        return (value < 0) ? -1 : 1;
    }

    /**
     * Computes the floored division <code>dividend/divisor</code> which
     * is useful when dividing potentially negative numbers into bins. For
     * positive numbers, it is the same as normal division, for negative
     * numbers it returns <code>(dividend - divisor + 1) / divisor</code>.
     *
     * <p> For example, the following numbers floorDiv 10 are:
     * <pre>
     * -15 -10 -8 -2 0 2 8 10 15
     *  -2  -1 -1 -1 0 0 0  1  1
     * </pre>
     */
    public static int floorDiv (int dividend, int divisor)
    {
        return ((dividend >= 0) ? dividend : (dividend - divisor + 1))/divisor;
    }
}
