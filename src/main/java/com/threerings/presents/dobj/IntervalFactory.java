package com.threerings.presents.dobj;

import com.samskivert.util.Interval;

public interface IntervalFactory
{
    /**
     * Creates an {@link Interval} that runs the supplied runnable.
     *
     * <pre>
     * _factory.newInterval(someRunnable).schedule(500); // one shot
     * Interval ival = _factory.newInterval(someRunnable).schedule(500, true); // repeater
     * </pre>
     */
    Interval newInterval (Runnable action);
}
