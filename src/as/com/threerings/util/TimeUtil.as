//
// $Id: TimeUtil.java 3372 2005-03-01 01:16:01Z ray $
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

package com.threerings.util {

/**
 * Utility for times.
 */
public class TimeUtil
{
    /** Time unit constant. */
    public static const MILLISECOND :int = 0;

    /** Time unit constant. */
    public static const SECOND :int = 1;

    /** Time unit constant. */
    public static const MINUTE :int = 2;

    /** Time unit constant. */
    public static const HOUR :int = 3;

    /** Time unit constant. */
    public static const DAY :int = 4;

    // TODO: Weeks?, months?
    protected static const MAX_UNIT :int = DAY;

    /**
     * Get a translatable string specifying the magnitude of the specified
     * duration. Results will be between "1 second" and "X hours", with
     * all times rounded to the nearest unit. "0 units" will never be
     * displayed, the minimum is 1.
     */
    public static function getTimeOrderString (
        duration :long, minUnit :int, maxUnit :int = -1) :String
    {
        // enforce sanity
        if (maxUnit == -1) {
            maxUnit = MAX_UNIT;
        }
        minUnit = Math.min(minUnit, maxUnit);
        maxUnit = Math.min(maxUnit, MAX_UNIT);

        for (var uu :int = MILLISECOND; uu <= MAX_UNIT; uu++) {
            var quantity :int = getQuantityPerUnit(uu);
            if ((minUnit <= uu) && (duration < quantity || maxUnit == uu)) {
                duration = Math.max(1, duration);
                return MessageBundle.tcompose(getTransKey(uu),
                        String(duration));
            }
            duration = Math.round(duration / quantity);
        }

        // will not happen, because eventually uu will be MAX_UNIT
        return null;
    }

    /**
     * Get a translatable string specifying the duration, down to the
     * minimum granularity.
     */
    public static function getTimeString (duration :long, minUnit :int) :String
    {
        // sanity
        minUnit = Math.min(minUnit, MAX_UNIT);
        duration = Math.abs(duration);

        var list :Array = new Array();
        var parts :int = 0; // how many parts are in the translation string?
        for (var uu :int = MILLISECOND; uu <= MAX_UNIT; uu++) {
            var quantity :int = getQuantityPerUnit(uu);
            if (minUnit <= uu) {
                list.push(MessageBundle.tcompose(getTransKey(uu),
                        String(duration % quantity)));
                parts++;
            }
            duration /= quantity;
            if (duration <= 0 && parts > 0) {
                break;
            }
        }

        if (parts == 1) {
            return (list[0] as String);
        } else {
            return MessageBundle.compose("m.times_" + parts, list);
        }
    }

    /**
     * Internal method to get the quantity for the specified unit.
     * (Not very OO)
     */
    protected static function getQuantityPerUnit (unit :int) :int
    {
        switch (unit) {
        case MILLISECOND: return 1000;
        case SECOND: case MINUTE: return 60;
        case HOUR: return 24;
        case DAY: return Integer.MAX_VALUE;
        default: return -1;
        }
    }

    /**
     * Internal method to get the translation key for the specified unit.
     * (Not very OO)
     */
    protected static function getTransKey (unit :int) :String
    {
        switch (unit) {
        case MILLISECOND: return "m.millisecond";
        case SECOND: return "m.second";
        case MINUTE: return "m.minute";
        case HOUR: return "m.hour";
        case DAY: return "m.day";
        default: return null;
        }
    }
}
}
