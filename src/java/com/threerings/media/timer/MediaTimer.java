//
// $Id: MediaTimer.java,v 1.1 2002/11/20 02:17:38 mdb Exp $

package com.threerings.media.timer;

/**
 * Provides a pluggable mechanism for delivering high resolution timing
 * information. The timers are not intended to be used by different
 * threads and thus must be protected by synchronization in such
 * circumstances.
 */
public interface MediaTimer
{
    /**
     * Resets the timer's monotonically increasing value.
     */
    public void reset ();

    /**
     * Returns the number of milliseconds that have elapsed since the
     * timer was created or last {@link #reset}. <em>Note:</em> the
     * accuracy of this method is highly dependent on the timer
     * implementation used.
     */
    public long getElapsedMillis ();

    /**
     * Returns the number of microseconds that have elapsed since the
     * timer was created or last {@link #reset}. <em>Note:</em> the
     * accuracy of this method is highly dependent on the timer
     * implementation used.
     */
    public long getElapsedMicros ();
}
