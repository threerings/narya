//
// $Id: DirectionUtil.java,v 1.4 2002/05/17 21:12:14 mdb Exp $

package com.threerings.util;

import java.awt.Point;

/**
 * Direction related utility functions.
 */
public class DirectionUtil implements DirectionCodes
{
    /**
     * Returns an array of names corresponding to each direction constant.
     */
    public static String[] getDirectionNames ()
    {
        return DIR_STRINGS;
    }

    /**
     * Returns a string representation of the supplied direction code.
     */
    public static String toString (int direction)
    {
        return ((direction >= SOUTHWEST) && (direction <= SOUTH)) ?
            DIR_STRINGS[direction] : "INVALID";
    }

    /**
     * Returns an abbreviated string representation of the supplied
     * direction code.
     */
    public static String toShortString (int direction)
    {
        return ((direction >= SOUTHWEST) && (direction <= SOUTH)) ?
            SHORT_DIR_STRINGS[direction] : "?";
    }

    /**
     * Returns a string representation of an array of direction codes. The
     * directions are represented by the abbreviated names.
     */
    public static String toString (int[] directions)
    {
        StringBuffer buf = new StringBuffer("{");
        for (int i = 0; i < directions.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(toShortString(directions[i]));
        }
        return buf.append("}").toString();
    }

    /**
     * Returns the direction that point <code>b</code> lies in from point
     * <code>a</code> as one of the {@link DirectionCodes} direction
     * constants.
     */
    public static int getDirection (Point a, Point b)
    {
        return getDirection(a.x, a.y, b.x, b.y);
    }

    /**
     * Returns the direction that point <code>b</code> lies in from point
     * <code>a</code> as one of the {@link DirectionCodes} direction
     * constants.
     */
    public static int getDirection (int ax, int ay, int bx, int by)
    {
        if (ax == bx && ay > by) {
            return NORTH;
        } else if (ax == bx && ay < by) {
            return SOUTH;

        } else if (ax < bx && ay > by) {
            return NORTHEAST;
        } else if (ax < bx && ay == by) {
            return EAST;
        } else if (ax < bx && ay < by) {
            return SOUTHEAST;

        } else if (ax > bx && ay < by) {
            return SOUTHWEST;
        } else if (ax > bx && ay == by) {
            return WEST;
        } else if (ax > bx && ay > by) {
            return NORTHWEST;

        } else {
            return NONE;
        }
    }

    /** Direction constant string names. */
    protected static final String[] DIR_STRINGS = {
        "SOUTHWEST", "WEST", "NORTHWEST", "NORTH",
        "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH"
    };

    /** Abbreviated direction constant string names. */
    protected static final String[] SHORT_DIR_STRINGS = {
        "SW", "W", "NW", "N", "NE", "E", "SE", "S"
    };
}
