//
// $Id: MathUtil.java,v 1.5 2002/09/17 04:00:09 mdb Exp $

package com.threerings.media.util;

import com.threerings.media.Log;

import java.awt.Point;

/**
 * Provides miscellaneous useful utility routines for mathematical
 * calculations.
 */
public class MathUtil
{
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
}
