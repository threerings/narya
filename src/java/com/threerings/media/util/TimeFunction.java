//
// $Id: TimeFunction.java,v 1.1 2004/09/18 22:57:08 mdb Exp $

package com.threerings.media.util;

/**
 * Used to vary a value over time where time is provided at discrete
 * increments (on the frame tick) and the value is computed appropriately.
 */
public abstract class TimeFunction
{
    /**
     * Every time function varies a value from some starting value to some
     * ending value over some duration. The way in which it varies
     * (linearly, for example) is up to the derived class.
     *
     * <p><em>Note:</em> it is assumed that we will operate with
     * relatively short durations such that integer arithmetic may be used
     * rather than long arithmetic.
     */
    public TimeFunction (int start, int end, int duration)
    {
        _start = start;
        _end = end;
        _duration = duration;
    }

    /**
     * Configures this function with a starting time. This method need not
     * be called, and instead the first vall to {@link #getValue} will be
     * used to obtain a starting time stamp.
     */
    public void init (long tickStamp)
    {
        _startStamp = tickStamp;
    }

    /**
     * Called to fast forward our time stamps if we are ever paused and
     * need to resume where we left off.
     */
    public void fastForward (long timeDelta)
    {
        _startStamp += timeDelta;
    }

    /**
     * Returns the current value given the supplied time stamp. The value
     * will be bounded to the originally supplied starting and ending
     * values at times 0 (and below) and {@link #_duration} (and above)
     * respectively.
     */
    public int getValue (long tickStamp)
    {
        if (_startStamp == 0L) {
            _startStamp = tickStamp;
        }

        int dt = (int)(tickStamp - _startStamp);
        if (dt <= 0) {
            return _start;
        } else if (dt >= _duration) {
            return _end;
        } else {
            return computeValue(dt);
        }
    }

    /**
     * This must be implemented by our derived class to compute our value
     * given the specified elapsed time (in millis).
     */
    protected abstract int computeValue (int dt);

    /** Our starting and ending values. */
    protected int _start, _end;

    /** The number of milliseconds over which we vary our value. */
    protected int _duration;

    /** The timestamp at which we began varying our value. */
    protected long _startStamp;
}
