//
// $Id: DirectionUtil.java,v 1.2 2001/12/17 03:43:54 mdb Exp $

package com.threerings.util;

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
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < directions.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(toShortString(directions[i]));
        }
        return buf.toString();
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
