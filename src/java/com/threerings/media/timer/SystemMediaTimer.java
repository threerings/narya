//
// $Id: SystemMediaTimer.java,v 1.4 2003/08/08 21:41:28 mdb Exp $

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
 *
 * <p> <em>Also note:</em> clock drift on Windows XP is especially
 * pronounced and is exacerbated by the fact that WinXP periodically
 * resyncs the system clock with the hardware clock, causing discontinuous
 * jumps in the progression of time (usually backwards in time).
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
        return System.currentTimeMillis() - _resetStamp;
    }

    // documentation inherited from interface
    public long getElapsedMicros ()
    {
        return getElapsedMillis() * 10;
    }

    /** The time at which this timer was last reset. */
    protected long _resetStamp = System.currentTimeMillis();
}
