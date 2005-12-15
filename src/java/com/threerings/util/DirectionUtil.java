//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util;

import java.awt.Point;

import com.samskivert.util.IntListUtil;

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
        return ((direction >= 0) && (direction < FINE_DIRECTION_COUNT)) ?
            DIR_STRINGS[direction] : "INVALID";
    }

    /**
     * Returns an abbreviated string representation of the supplied
     * direction code.
     */
    public static String toShortString (int direction)
    {
        return ((direction >= 0) && (direction < FINE_DIRECTION_COUNT)) ?
            SHORT_DIR_STRINGS[direction] : "?";
    }

    /**
     * Returns the direction code that corresponds to the supplied string
     * or {@link #NONE} if the string does not correspond to a known
     * direction code.
     */
    public static int fromString (String dirstr)
    {
        for (int ii = 0; ii < FINE_DIRECTION_COUNT; ii++) {
            if (DIR_STRINGS[ii].equals(dirstr)) {
                return ii;
            }
        }
        return NONE;
    }

    /**
     * Returns the direction code that corresponds to the supplied short
     * string or {@link #NONE} if the string does not correspond to a
     * known direction code.
     */
    public static int fromShortString (String dirstr)
    {
        for (int ii = 0; ii < FINE_DIRECTION_COUNT; ii++) {
            if (SHORT_DIR_STRINGS[ii].equals(dirstr)) {
                return ii;
            }
        }
        return NONE;
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
     * Rotates the requested <em>fine</em> direction constant clockwise by
     * the requested number of ticks.
     */
    public static int rotateCW (int direction, int ticks)
    {
        for (int ii = 0; ii < ticks; ii++) {
            direction = FINE_CW_ROTATE[direction];
        }
        return direction;
    }

    /**
     * Rotates the requested <em>fine</em> direction constant
     * counter-clockwise by the requested number of ticks.
     */
    public static int rotateCCW (int direction, int ticks)
    {
        for (int ii = 0; ii < ticks; ii++) {
            direction = FINE_CCW_ROTATE[direction];
        }
        return direction;
    }

    /**
     * Returns the opposite of the specified direction.
     */
    public static int getOpposite (int direction)
    {
        return rotateCW(direction, FINE_CW_ROTATE.length/2);
    }

    /**
     * Get the direction closest to the specified direction, out of
     * the directions in the possible list (preferring a clockwise match).
     */
    public static int getClosest (int direction, int[] possible)
    {
        return getClosest(direction, possible, true);
    }

    /**
     * Get the direction closest to the specified direction, out of
     * the directions in the possible list.
     *
     * @param preferCW whether to prefer a clockwise match or a
     * counter-clockwise match.
     */
    public static int getClosest (int direction, int[] possible,
            boolean preferCW)
    {
        // rotate a tick at a time, looking for matches
        int first = direction;
        int second = direction;
        for (int ii = 0; ii <= FINE_DIRECTION_COUNT / 2; ii++) {
            if (IntListUtil.contains(possible, first)) {
                return first;
            }

            if (ii != 0 && IntListUtil.contains(possible, second)) {
                return second;
            }

            first = preferCW ? rotateCW(first, 1) : rotateCCW(first, 1);
            second = preferCW ? rotateCCW(second, 1) : rotateCW(second, 1);
        }

        return NONE;
    }

    /**
     * Returns which of the eight compass directions that point
     * <code>b</code> lies in from point <code>a</code> as one of the
     * {@link DirectionCodes} direction constants. <em>Note:</em> that the
     * coordinates supplied are assumed to be logical (screen) rather than
     * cartesian coordinates and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getDirection (Point a, Point b)
    {
        return getDirection(a.getX(), a.getY(), b.getX(), b.getY());
    }

    /**
     * Returns which of the eight compass directions that point
     * <code>b</code> lies in from point <code>a</code> as one of the
     * {@link DirectionCodes} direction constants. <em>Note:</em> that the
     * coordinates supplied are assumed to be logical (screen) rather than
     * cartesian coordinates and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getDirection (int ax, int ay, int bx, int by)
    {
        return getDirection(Math.atan2(by-ay, bx-ax));
    }

    /**
     * Returns which of the eight compass directions that point
     * <code>b</code> lies in from point <code>a</code> as one of the
     * {@link DirectionCodes} direction constants. <em>Note:</em> that the
     * coordinates supplied are assumed to be logical (screen) rather than
     * cartesian coordinates and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getDirection (double ax, double ay, double bx, double by)
    {
        return getDirection(Math.atan2(by-ay, bx-ax));
    }

    /**
     * Returns which of the eight compass directions is associated with
     * the specified angle theta. <em>Note:</em> that the angle supplied
     * is assumed to increase clockwise around the origin (which screen
     * angles do) rather than counter-clockwise around the origin (which
     * cartesian angles do) and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getDirection (double theta)
    {
        theta = ((theta + Math.PI) * 4) / Math.PI;
        return (int)(Math.round(theta) + WEST) % 8;
    }

    /**
     * Returns which of the sixteen compass directions that point
     * <code>b</code> lies in from point <code>a</code> as one of the
     * {@link DirectionCodes} direction constants. <em>Note:</em> that the
     * coordinates supplied are assumed to be logical (screen) rather than
     * cartesian coordinates and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getFineDirection (Point a, Point b)
    {
        return getFineDirection(a.x, a.y, b.x, b.y);
    }

    /**
     * Returns which of the sixteen compass directions that point
     * <code>b</code> lies in from point <code>a</code> as one of the
     * {@link DirectionCodes} direction constants. <em>Note:</em> that the
     * coordinates supplied are assumed to be logical (screen) rather than
     * cartesian coordinates and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getFineDirection (int ax, int ay, int bx, int by)
    {
        return getFineDirection(Math.atan2(by-ay, bx-ax));
    }

    /**
     * Returns which of the sixteen compass directions is associated with
     * the specified angle theta. <em>Note:</em> that the angle supplied
     * is assumed to increase clockwise around the origin (which screen
     * angles do) rather than counter-clockwise around the origin (which
     * cartesian angles do) and <code>NORTH</code> is considered to point
     * toward the top of the screen.
     */
    public static int getFineDirection (double theta)
    {
        theta = ((theta + Math.PI) * 8) / Math.PI;
        return ANGLE_MAP[(int)Math.round(theta) % FINE_DIRECTION_COUNT];
    }

    /**
     * Move the specified point in the specified screen direction,
     * adjusting by the specified adjustments. Fine directions are
     * not supported.
     */
    public static void moveDirection (Point p, int direction, int dx, int dy)
    {
        if (direction >= DIRECTION_COUNT) {
            throw new IllegalArgumentException(
                "Fine coordinates not supported.");
        }

        switch (direction) {
            case NORTH: case NORTHWEST: case NORTHEAST: p.y -= dy;
        }
        switch (direction) {
            case SOUTH: case SOUTHWEST: case SOUTHEAST: p.y += dy;
        }
        switch (direction) {
            case WEST: case SOUTHWEST: case NORTHWEST: p.x -= dx;
        }
        switch (direction) {
            case EAST: case SOUTHEAST: case NORTHEAST: p.x += dx;
        }
    }

    /** Direction constant string names. */
    protected static final String[] DIR_STRINGS = {
        "SOUTHWEST", "WEST", "NORTHWEST", "NORTH",
        "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH",
        "WESTSOUTHWEST", "WESTNORTHWEST", "NORTHNORTHWEST", "NORTHNORTHEAST",
        "EASTNORTHEAST", "EASTSOUTHEAST", "SOUTHSOUTHEAST", "SOUTHSOUTHWEST",
    };

    /** Abbreviated direction constant string names. */
    protected static final String[] SHORT_DIR_STRINGS = {
        "SW", "W", "NW", "N", "NE", "E", "SE", "S",
        "WSW", "WNW", "NNW", "NNE", "ENE", "ESE", "SSE", "SSW",
    };

    /** Used to rotate a fine compass direction clockwise. */
    protected static final int[] FINE_CW_ROTATE = {
        /* SW -> */ WESTSOUTHWEST,  /* W -> */ WESTNORTHWEST,
        /* NW -> */ NORTHNORTHWEST, /* N -> */ NORTHNORTHEAST,
        /* NE -> */ EASTNORTHEAST,  /* E -> */ EASTSOUTHEAST,
        /* SE -> */ SOUTHSOUTHEAST, /* S -> */ SOUTHSOUTHWEST,
        /* WSW -> */ WEST,          /* WNW -> */ NORTHWEST,
        /* NNW -> */ NORTH,         /* NNE -> */ NORTHEAST,
        /* ENE -> */ EAST,          /* ESE -> */ SOUTHEAST,
        /* SSE -> */ SOUTH,         /* SSW -> */ SOUTHWEST
    };

    /** Used to rotate a fine compass direction counter-clockwise. */
    protected static final int[] FINE_CCW_ROTATE = {
        /* SW -> */ SOUTHSOUTHWEST, /* W -> */ WESTSOUTHWEST,
        /* NW -> */ WESTNORTHWEST,  /* N -> */ NORTHNORTHWEST,
        /* NE -> */ NORTHNORTHEAST, /* E -> */ EASTNORTHEAST,
        /* SE -> */ EASTSOUTHEAST,  /* S -> */ SOUTHSOUTHEAST,
        /* WSW -> */ SOUTHWEST,     /* WNW -> */ WEST,
        /* NNW -> */ NORTHWEST,     /* NNE -> */ NORTH,
        /* ENE -> */ NORTHEAST,     /* ESE -> */ EAST,
        /* SSE -> */ SOUTHEAST,     /* SSW -> */ SOUTH
    };                                                            

    /** Used to map an angle to a fine compass direction. */
    protected static final int[] ANGLE_MAP = {
        WEST, WESTNORTHWEST, NORTHWEST, NORTHNORTHWEST, NORTH, NORTHNORTHEAST,
        NORTHEAST, EASTNORTHEAST, EAST, EASTSOUTHEAST, SOUTHEAST,
        SOUTHSOUTHEAST, SOUTH, SOUTHSOUTHWEST, SOUTHWEST, WESTSOUTHWEST };
}                                                                       
