//
// $Id: TimeUtil.java,v 1.4 2004/08/27 02:20:36 mdb Exp $
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
 * Utility for times.
 */
public class TimeUtil
{
    /** Granularity constant. */
    public static final byte MILLISECOND = 0;

    /** Granularity constant. */
    public static final byte SECOND = 1;

    /** Granularity constant. */
    public static final byte MINUTE = 2;

    /** Granularity constant. */
    public static final byte HOUR = 3;

    /** Granularity constant. */
    public static final byte DAY = 4;

    // TODO: Weeks?, months?

    /**
     * Get a translatable string specifying the magnitude of the specified
     * duration. Results will be between "1 second" and "X hours", with
     * all times rounded to the nearest unit.
     */
    public static String getTimeOrderString (
        long duration, byte minGranularity)
    {
        return getTimeOrderString(duration, minGranularity, Byte.MAX_VALUE);
    }

    /**
     * Get a translatable string specifying the magnitude of the specified
     * duration, with the units of time bounded between the minimum and
     * maximum specified.
     */
    public static String getTimeOrderString (
        long duration, byte minGranularity, byte maxGranularity)
    {
        // enforce sanity
        minGranularity = (byte) Math.min(minGranularity, maxGranularity);

        if (minGranularity == MILLISECOND) {
            if (duration < 2) {
                return "m.1millisecond";
            } else if (duration < 1000 || maxGranularity == MILLISECOND) {
                return MessageBundle.tcompose("m.milliseconds",
                    String.valueOf(duration));
            }
        }

        int seconds = (int) Math.round(duration / 1000f);
        if (minGranularity <= SECOND) {
            if (seconds < 2) {
                return "m.1second";
            } else if (seconds < 60 || maxGranularity == SECOND) {
                return MessageBundle.tcompose("m.seconds",
                    String.valueOf(seconds));
            }
        }

        int minutes = (int) Math.round(seconds / 60f);
        if (minGranularity <= MINUTE) {
            if (minutes < 2) {
                return "m.1minute";
            } else if (minutes < 60 || maxGranularity == MINUTE) {
                return MessageBundle.tcompose("m.minutes",
                    String.valueOf(minutes));
            }
        }

        int hours = (int) Math.round(minutes / 60f);
        if (minGranularity <= HOUR) {
            if (hours < 2) {
                return "m.1hour";
            } else if (hours < 24 || maxGranularity == HOUR) {
                return MessageBundle.tcompose("m.hours", String.valueOf(hours));
            }
        }

        int days = (int) Math.round(hours / 24f);
        if (days < 2) {
            return "m.1day";
        } else {
            return MessageBundle.tcompose("m.days", String.valueOf(days));
        }

        // TODO: weeks? months? 
    }
}
