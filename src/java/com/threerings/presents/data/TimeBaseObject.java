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
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>evenBase</code> field. */
    public static final String EVEN_BASE = "evenBase";

    /** The field name of the <code>oddBase</code> field. */
    public static final String ODD_BASE = "oddBase";
    // AUTO-GENERATED: FIELDS END

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

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>evenBase</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setEvenBase (long value)
    {
        long ovalue = this.evenBase;
        requestAttributeChange(
            EVEN_BASE, new Long(value), new Long(ovalue));
        this.evenBase = value;
    }

    /**
     * Requests that the <code>oddBase</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setOddBase (long value)
    {
        long ovalue = this.oddBase;
        requestAttributeChange(
            ODD_BASE, new Long(value), new Long(ovalue));
        this.oddBase = value;
    }
    // AUTO-GENERATED: METHODS END
}
