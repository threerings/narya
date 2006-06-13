//
// $Id$

package com.threerings.media.util;

/**
 * Varies a value linearly with time.
 */
public class LinearTimeFunction extends TimeFunction
{
    public LinearTimeFunction (int start, int end, int duration)
    {
        super(start, end, duration);
    }

    // documentation inherited
    protected int computeValue (int dt)
    {
        int dv = (_end - _start);
        return (dt * dv / _duration) + _start;
    }
}
