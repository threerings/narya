//
// $Id: TimeUtil.java,v 1.2 2004/06/04 22:14:11 ray Exp $

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
        if (granularity <= HOUR) {
            if (hours < 2) {
                return "m.1hour";
            } else if (hours < 24) {
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
