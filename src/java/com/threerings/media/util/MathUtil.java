//
// $Id: MathUtil.java,v 1.1 2001/07/16 00:45:07 shaper Exp $

package com.threerings.cocktail.miso.util;

import com.threerings.cocktail.miso.Log;

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
}
