//
// $Id: SystemMediaTimer.java,v 1.2 2003/07/29 00:27:32 mdb Exp $

package com.threerings.media.timer;

import com.threerings.media.Log;

/**
 * Implements the {@link MediaTimer} interface using {@link
 * System#currentTimeMillis} to obtain timing information.
 *
 * <p> <em>Note:</em> {@link System#currentTimeMillis} is notoriously
 * inaccurate on different platforms. See <a
 * href="http://developer.java.sun.com/developer/bugParade/bugs/4486109.html">
 * bug report 4486109</a> for more information.
 */
public class SystemMediaTimer implements MediaTimer
{
    // documentation inherited from interface
    public void reset ()
    {
        _resetStamp = System.currentTimeMillis();
    }

    // documentation inherited from interface
    public long getElapsedMillis ()
    {
        long stamp = System.currentTimeMillis() - _resetStamp;

        // on WinXP the time sometimes seems to leap into the
        // past; here we do our best to work around this insanity
        if (stamp < _lastStamp) {
            // only warn once per time anomaly
            if (stamp > _lastWarning) {
                Log.warning("Someone call Einstein! The clock is " +
                            "running backwards [dt=" +
                            (stamp - _lastStamp) + "].");
                _lastWarning = _lastStamp;
            }
            stamp = _lastStamp;
        }
        _lastStamp = stamp;

        return stamp;
    }

    // documentation inherited from interface
    public long getElapsedMicros ()
    {
        return getElapsedMillis() * 10;
    }

    /** The time at which this timer was last reset. */
    protected long _resetStamp = System.currentTimeMillis();

    /** Used to ensure that the timer is sane. */
    protected long _lastStamp, _lastWarning;
}
