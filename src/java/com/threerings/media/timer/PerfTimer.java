//
// $Id: PerfTimer.java,v 1.1 2003/08/08 21:40:22 mdb Exp $

package com.threerings.media.timer;

import sun.misc.Perf;

import com.threerings.media.Log;

/**
 * A timer that uses the performance clock exposed by Sun in JDK 1.4.2.
 */
public class PerfTimer implements MediaTimer
{
    public PerfTimer ()
    {
        _timer = Perf.getPerf();
        _frequency = _timer.highResFrequency();
        _startStamp = _timer.highResCounter() * 1000 / _frequency;
        Log.info("Using high performance timer [freq=" + _frequency +
                 ", start=" + _startStamp + "].");
    }

    // documentation inherited from interface
    public void reset ()
    {
        _startStamp = _timer.highResCounter() * 1000 / _frequency;
    }

    // documentation inherited from interface
    public long getElapsedMillis ()
    {
        return _timer.highResCounter() * 1000 / _frequency;
    }

    // documentation inherited from interface
    public long getElapsedMicros ()
    {
        return _timer.highResCounter() * 100 / _frequency;
    }

    /** A performance timer object. */
    protected Perf _timer;

    /** The time at which this timer was last reset. */
    protected long _startStamp;

    /** The timer frequency. */
    protected long _frequency;
}
