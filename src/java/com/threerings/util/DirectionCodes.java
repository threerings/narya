//
// $Id: DirectionCodes.java,v 1.1 2001/12/17 03:32:32 mdb Exp $

package com.threerings.util;

/**
 * A single, top-level location for the definition of compass direction
 * constants, which are used by a variety of Narya services.
 */
public interface DirectionCodes
{
    /** A direction code indicating no direction. */
    public static final int NONE = -1;

    /** A direction code indicating southwest. */
    public static final int SOUTHWEST = 0;

    /** A direction code indicating west. */
    public static final int WEST = 1;

    /** A direction code indicating northwest. */
    public static final int NORTHWEST = 2;

    /** A direction code indicating north. */
    public static final int NORTH = 3;

    /** A direction code indicating northeast. */
    public static final int NORTHEAST = 4;

    /** A direction code indicating east. */
    public static final int EAST = 5;

    /** A direction code indicating southeast. */
    public static final int SOUTHEAST = 6;

    /** A direction code indicating south. */
    public static final int SOUTH = 7;

    /** The total number of directions. */
    public static final int DIRECTION_COUNT = 8;
}
