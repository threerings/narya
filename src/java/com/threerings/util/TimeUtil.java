//
// $Id: TimeUtil.java,v 1.1 2002/10/30 01:47:12 ray Exp $

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

    /**
     * Get a translatable string specifying the magnitude of the specified
     * duration. Results will be between "1 second" and "X hours", with
     * all times rounded to the nearest unit.
     */
    public static String getTimeOrderString (long duration, byte granularity)
    {
        if (granularity == MILLISECOND) {
            if (duration < 2) {
                return "m.1millisecond";
            } else if (duration < 1000) {
                return MessageBundle.tcompose("m.milliseconds",
                    String.valueOf(duration));
            }
        }

        int seconds = (int) Math.round(duration / 1000f);

        if (granularity <= SECOND) {
            if (seconds < 2) {
                return "m.1second";
            } else if (seconds < 60) {
                return MessageBundle.tcompose("m.seconds",
                    String.valueOf(seconds));
            }
        }

        int minutes = (int) Math.round(seconds / 60f);

        if (granularity <= MINUTE) {
            if (minutes < 2) {
                return "m.1minute";
            } else if (minutes < 60) {
                return MessageBundle.tcompose("m.minutes",
                    String.valueOf(minutes));
            }
        }

        int hours = (int) Math.round(minutes / 60f);

        if (hours < 2) {
            return "m.1hour";
        } else {
            return MessageBundle.tcompose("m.hours", String.valueOf(hours));
        }

        // TODO: days? weeks? months? 
    }
}
