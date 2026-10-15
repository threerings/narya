//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.util.HashMap;

import java.util.concurrent.Executor;

import com.samskivert.util.Histogram;
import com.samskivert.util.Invoker;
import com.samskivert.util.StringUtil;

/**
 * Extends invoker with a reporter implementation that shows current queue status, maximum
 * historical size and the results of unit profiling if enabled.
 */
public class ReportingInvoker extends Invoker
{
    public static class Stats
    {
        public int unitsRun;
        public int maxQueueSize;
    }

    /**
     * Creates a new reporting invoker. The instance will be registered with the report manager
     * if performance tracking is enabled (see {@link Invoker#setPerfTrack}).
     */
    public ReportingInvoker (String name, Executor receiver, ReportManager repmgr)
    {
        super(name, receiver);
        if (_perfTrack) {
            repmgr.registerReporter(ReportManager.DEFAULT_TYPE, _defrep);
            repmgr.registerReporter(ReportManager.PROFILE_TYPE, _profrep);
        }
    }

    /**
     * Returns a recent snapshot of runtime statistics tracked by the invoker.
     *
     * @param snapshot if true, the current stats will be snapshotted and reset and the new
     * snapshot will be returned. If false, the previous snapshot will be returned. If no snapshot
     * has ever been taken, the current stats that have been accumulting since the JVM start will
     * be returned.
     */
    public Stats getStats (boolean snapshot)
    {
        synchronized (this) {
            if (snapshot) {
                _recent = _current;
                _current = new Stats();
                _current.maxQueueSize = _queue.size();
            }
            return _recent;
        }
    }

    /**
     * Sets the parameters of the unit profiling histogram. This only affects unit classes that
     * have not yet been run, so should be called early on in program initialization.
     * @param bucketWidthMs size of time buckets, in milliseconds
     * @param bucketCount number of time buckets
     */
    public void setProfilingParameters (int bucketWidthMs, int bucketCount)
    {
        _profileBucketWidth = bucketWidthMs;
        _profileBucketCount = bucketCount;
    }

    @Override // from Invoker
    protected void willInvokeUnit (Unit unit, long start)
    {
        super.willInvokeUnit(unit, start);

        if (_perfTrack) {
            // record the time this unit spent waiting on the queue
            recordMetrics("queue_wait_time", start - unit.queueStamp);
        }

        int queueSize = _queue.size();
        synchronized (this) {
            // keep track of the largest queue size we've seen
            if (queueSize > _current.maxQueueSize) {
                _current.maxQueueSize = queueSize;
            }
            _current.unitsRun++;
            _totalUnitsRun++;

            // note the currently invoking unit
            _currentUnit = unit;
            _currentUnitStart = start;
        }
    }

    @Override // from Invoker
    protected void didInvokeUnit (Unit unit, long start)
    {
        super.didInvokeUnit(unit, start);

        if (_perfTrack) {
            // profile this unit's run time, keyed on its class
            recordMetrics(unit.getClass(), System.currentTimeMillis() - start);
        }

        synchronized (this) {
            // clear out our currently invoking unit
            _currentUnit = null;
            _currentUnitStart = 0L;
        }
    }

    protected void recordMetrics (Object key, long duration)
    {
        UnitProfile prof = _tracker.get(key);
        if (prof == null) {
            _tracker.put(key, prof = new UnitProfile(_profileBucketWidth, _profileBucketCount));
        }
        prof.record(duration);
    }

    /** Generates a report on our runtime behavior. */
    protected ReportManager.Reporter _defrep = new ReportManager.Reporter() {
        public void appendReport (StringBuilder buf, long now, long sinceLast, boolean reset) {
            buf.append("* " + getName() + ":\n");
            int qsize = _queue.size();
            buf.append("- Queue size: ").append(qsize).append("\n");
            synchronized (this) {
                Stats stats = getStats(reset);
                buf.append("- Max queue size: ").append(stats.maxQueueSize).append("\n");
                buf.append("- Units executed: ").append(stats.unitsRun).append("\n");
                buf.append("- Total units executed: ").append(_totalUnitsRun);
                long runPerSec = stats.unitsRun/Math.max(1, sinceLast/1000);
                buf.append(" (").append(runPerSec).append("/s)\n");
                if (_currentUnit != null) {
                    String uname = StringUtil.safeToString(_currentUnit);
                    buf.append("- Current unit: ").append(uname).append(" ");
                    buf.append(now-_currentUnitStart).append("ms\n");
                }
            }
        }
    };

    /** Generates a report with our profiling data. */
    protected ReportManager.Reporter _profrep = new ReportManager.Reporter() {
        public void appendReport (StringBuilder buf, long now, long sinceLast, boolean reset) {
            buf.append("* " + getName() + ":\n");
            if (PresentsDObjectMgr.UNIT_PROF_ENABLED) {
                for (Object key : _tracker.keySet()) {
                    UnitProfile profile = _tracker.get(key);
                    if (key instanceof Class<?>) {
                        key = StringUtil.shortClassName((Class<?>)key);
                    }
                    buf.append("  ").append(key).append(" ");
                    buf.append(profile).append("\n");
                    if (reset) {
                        profile.clear();
                    }
                }
            } else {
                buf.append(" - Unit profiling disabled.\n");
            }
        }
    };

    /** Used to track profile information on invoked units. */
    protected static class UnitProfile
    {
        public UnitProfile (int bucketWidth, int bucketCount) {
            _histo = new Histogram(0, bucketWidth, bucketCount);
        }

        public void record (long duration) {
            _totalElapsed += duration;
            _histo.addValue((int)duration);
        }

        public void clear () {
            _totalElapsed = 0L;
            _histo.clear();
        }

        @Override public String toString () {
            int count = _histo.size();
            return _totalElapsed + "ms/" + count + " = " + (_totalElapsed/count) + "ms avg " +
                StringUtil.toString(_histo.getBuckets());
        }

        protected Histogram _histo;
        protected long _totalElapsed;
    }

    @Deprecated // TODO: Remove soon!
    protected boolean _perfTrack = true;

    /** Used to track runtime statistics. */
    protected Stats _recent = new Stats(), _current = _recent;

    /** The total number of units run. */
    protected int _totalUnitsRun;

    /** Records the currently invoking unit. */
    protected Object _currentUnit;

    /** The time at which our current unit started. */
    protected long _currentUnitStart;

    /** Tracks the counts of invocations by unit's class. */
    protected HashMap<Object,UnitProfile> _tracker = new HashMap<Object,UnitProfile>();

    /** Size of buckets to use when profiling unit times. */
    protected int _profileBucketWidth = 50;

    /** Number of buckets to use when profiling unit times. */
    protected int _profileBucketCount = 10;
}
