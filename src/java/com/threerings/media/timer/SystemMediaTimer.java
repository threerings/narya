//
// $Id: SystemMediaTimer.java,v 1.3 2003/07/29 00:41:40 mdb Exp $

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
