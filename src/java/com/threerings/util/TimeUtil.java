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

import java.util.ArrayList;

/**
 * Utility for times.
 */
public class TimeUtil
{
    /** Time unit constant. */
    public static final byte MILLISECOND = 0;

    /** Time unit constant. */
    public static final byte SECOND = 1;

    /** Time unit constant. */
    public static final byte MINUTE = 2;

    /** Time unit constant. */
    public static final byte HOUR = 3;

    /** Time unit constant. */
    public static final byte DAY = 4;

    // TODO: Weeks?, months?
    protected static final byte MAX_UNIT = DAY;

    /**
     * Get a translatable string specifying the magnitude of the specified
     * duration. Results will be between "1 second" and "X hours", with
     * all times rounded to the nearest unit. "0 units" will never be
     * displayed, the minimum is 1.
     */
    public static String getTimeOrderString (long duration, byte minUnit)
    {
        return getTimeOrderString(duration, minUnit, MAX_UNIT);
    }

    /**
     * Get a translatable string specifying the magnitude of the specified
     * duration, with the units of time bounded between the minimum and
     * maximum specified. "0 units" will never be returned, the minimum is 1.
     */
    public static String getTimeOrderString (
        long duration, byte minUnit, byte maxUnit)
    {
        // enforce sanity
        minUnit = (byte) Math.min(minUnit, maxUnit);
        maxUnit = (byte) Math.min(maxUnit, MAX_UNIT);

        for (byte uu = MILLISECOND; uu <= MAX_UNIT; uu++) {
            int quantity = getQuantityPerUnit(uu);
            if ((minUnit <= uu) && (duration < quantity || maxUnit == uu)) {
                duration = Math.max(1, duration);
                return MessageBundle.tcompose(getTransKey(uu),
                        String.valueOf(duration));
            }
            duration = Math.round(duration / quantity);
        }

        // will not happen, because eventually gg will be MAX_UNIT
        Thread.dumpStack();
        return null;
    }

    /**
     * Get a translatable string specifying the duration, down to the
     * minimum granularity.
     */
    public static String getTimeString (long duration, byte minUnit)
    {
        // sanity
        minUnit = (byte) Math.min(minUnit, MAX_UNIT);
        duration = Math.abs(duration);

        ArrayList list = new ArrayList();
        int parts = 0; // how many parts are in the translation string?
        for (byte uu = MILLISECOND; uu <= MAX_UNIT; uu++) {
            int quantity = getQuantityPerUnit(uu);
            if (minUnit <= uu) {
                list.add(MessageBundle.tcompose(getTransKey(uu),
                            String.valueOf(duration % quantity)));
                parts++;
            }
            duration /= quantity;
            if (duration <= 0 && parts > 0) {
                break;
            }
        }

        if (parts == 1) {
            return (String) list.get(0);
        } else {
            return MessageBundle.compose("m.times_" + parts, list.toArray());
        }
    }

    /**
     * Internal method to get the quantity for the specified unit.
     * (Not very OO)
     */
    protected static int getQuantityPerUnit (byte unit)
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
    protected static String getTransKey (byte unit)
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
