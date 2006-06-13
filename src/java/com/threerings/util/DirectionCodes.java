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

/**
 * A single, top-level location for the definition of compass direction
 * constants, which are used by a variety of Narya services.
 */
public interface DirectionCodes
{
    /** A direction code indicating no direction. */
    public static final int NONE = -1;

    /** A direction code indicating moving left. */
    public static final int LEFT = 0;

    /** A direction code indicating moving right. */
    public static final int RIGHT = 1;

    /** A direction code indicating a counter-clockwise rotation. */
    public static final int CCW = 0;

    /** A direction code indicating a clockwise rotation. */
    public static final int CW = 1;

    /** A direction code indicating horizontal movement. */
    public static final int HORIZONTAL = 0;

    /** A direction code indicating vertical movement. */
    public static final int VERTICAL = 1;

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

    /** The number of basic compass directions. */
    public static final int DIRECTION_COUNT = 8;

    /** A direction code indicating west by southwest. */
    public static final int WESTSOUTHWEST = 8;

    /** A direction code indicating west by northwest. */
    public static final int WESTNORTHWEST = 9;

    /** A direction code indicating north by northwest. */
    public static final int NORTHNORTHWEST = 10;

    /** A direction code indicating north by northeast. */
    public static final int NORTHNORTHEAST = 11;

    /** A direction code indicating east by northeast. */
    public static final int EASTNORTHEAST = 12;

    /** A direction code indicating east by southeast. */
    public static final int EASTSOUTHEAST = 13;

    /** A direction code indicating south by southeast. */
    public static final int SOUTHSOUTHEAST = 14;

    /** A direction code indicating south by southwest. */
    public static final int SOUTHSOUTHWEST = 15;

    /** The number of fine compass directions. */
    public static final int FINE_DIRECTION_COUNT = 16;
}
