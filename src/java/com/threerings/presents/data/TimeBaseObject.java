//
// $Id: TimeBaseObject.java,v 1.2 2003/04/30 22:45:57 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.dobj.DObject;

/**
 * Used to communicate time bases to clients so that more efficient delta
 * times can be transmitted over the network. Two time stamps are
 * maintained: even and odd. When the even time base is sufficiently old
 * that our delta time container (usually a short or an int) will exceed
 * its maximum value, we switch to the odd time base. The bouncing between
 * two separate values prevents problems from arising when the base time
 * is changed, yet values using the old base time might still be
 * propagating through the system.
 *
 * <p> Note that for sufficiently small delta time containers, stale
 * values could still linger longer than the time required for two swaps
 * (two byte delta stamps for example, must swap every 30 seconds).
 */
public class TimeBaseObject extends DObject
{
    /** The field name of the <code>evenBase</code> field. */
    public static final String EVEN_BASE = "evenBase";

    /** The field name of the <code>oddBase</code> field. */
    public static final String ODD_BASE = "oddBase";

    /** The even time base, used to decode even delta times. */
    public long evenBase;

    /** The odd time base, used to decode odd delta times. */
    public long oddBase;

    /**
     * Converts the supplied time stamp into a time delta (measured
     * relative to the appropriate time base, even or odd) with maximum
     * value of 2^15. (One bit must be used to indicate that it is an even
     * or odd time stamp).
     */
    public short toShortDelta (long timeStamp)
    {
        return (short)getDelta(timeStamp, Short.MAX_VALUE);
    }

    /**
     * Converts the supplied time stamp into a time delta (measured
     * relative to the appropriate time base, even or odd) with maximum
     * value of 2^31. (One bit must be used to indicate that it is an even
     * or odd time stamp).
     */
    public int toIntDelta (long timeStamp)
    {
        return (int)getDelta(timeStamp, Integer.MAX_VALUE);
    }

    /**
     * Converts the supplied delta time back to a wall time based on the
     * base time in this time base object. Either an int or short delta
     * can be passed to this method (the short will have been promoted to
     * an int in the process but that will not mess up its encoded value).
     */
    public long fromDelta (int delta)
    {
        boolean even = (delta > 0);
        long time = even ? evenBase : oddBase;
        if (even) {
            time += delta;
        } else {
            time += (-1 - delta);
        }
        return time;
    }

    /**
     * Obtains a delta with the specified maximum value, swapping from
     * even to odd, if necessary.
     */
    protected long getDelta (long timeStamp, long maxValue)
    {
        boolean even = (evenBase > oddBase);
        long base = even ? evenBase : oddBase;
        long delta = timeStamp - base;

        // make sure this timestamp is not sufficiently old that we can't
        // generate a delta time with it
        if (delta < 0) {
            String errmsg = "Time stamp too old for conversion to delta time";
            throw new IllegalArgumentException(errmsg);
        }

        // see if it's time to swap
        if (delta > maxValue) {
            if (even) {
                setOddBase(timeStamp);
            } else {
                setEvenBase(timeStamp);
            }
            delta = 0;
        }

        // if we're odd, we need to mark the value as such
        if (!even) {
            delta = (-1 - delta);
        }

        return delta;
    }

    /**
     * Requests that the <code>evenBase</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setEvenBase (long evenBase)
    {
        requestAttributeChange(EVEN_BASE, new Long(evenBase));
        this.evenBase = evenBase;
    }

    /**
     * Requests that the <code>oddBase</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setOddBase (long oddBase)
    {
        requestAttributeChange(ODD_BASE, new Long(oddBase));
        this.oddBase = oddBase;
    }
}
