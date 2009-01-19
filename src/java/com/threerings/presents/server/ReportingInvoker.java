//
// $Id$

package com.threerings.presents.server;

import com.samskivert.util.Invoker;
import com.samskivert.util.RunQueue;
import com.samskivert.util.StringUtil;

/**
 * Extends invoker with a reporter implementation that shows current queue status, maximum
 * historical size and the results of unit profiling if enabled.
 */
public class ReportingInvoker extends Invoker
    implements ReportManager.Reporter
{
    /**
     * Creates a new reporting invoker. The instance will be registered with the report manager
     * if profiling is enabled ({@link Invoker#PERF_TRACK}).
     */
    public ReportingInvoker (String name, RunQueue resultsQueue, ReportManager repmgr)
    {
        super(name, resultsQueue);
        if (PERF_TRACK) {
            repmgr.registerReporter(this);
        }
    }

    // from interface ReportManager.Reporter
    public void appendReport (StringBuilder buf, long now, long sinceLast, boolean reset)
    {
        buf.append("* " + getName() + ":\n");
        int qsize = _queue.size();
        buf.append("- Queue size: ").append(qsize).append("\n");
        synchronized (this) {
            buf.append("- Max queue size: ").append(_maxQueueSize).append("\n");
            buf.append("- Units executed: ").append(_unitsRun);
            long runPerSec = (sinceLast == 0) ? 0 : 1000*_unitsRun/sinceLast;
            buf.append(" (").append(runPerSec).append("/s)\n");
            if (_currentUnit != null) {
                String uname = StringUtil.safeToString(_currentUnit);
                buf.append("- Current unit: ").append(uname).append(" ");
                buf.append(now-_currentUnitStart).append("ms\n");
            }
            if (reset) {
                _maxQueueSize = qsize;
                _unitsRun = 0;
            }
        }

        if (PresentsDObjectMgr.UNIT_PROF_ENABLED) {
            for (Object key : _tracker.keySet()) {
                UnitProfile profile = _tracker.get(key);
                if (key instanceof Class) {
                    key = StringUtil.shortClassName((Class<?>)key);
                }
                buf.append("  ").append(key).append(" ");
                buf.append(profile).append("\n");
                if (reset) {
                    profile.clear();
                }
            }
        }
    }

    @Override // from Invoker
    protected void willInvokeUnit (Unit unit, long start)
    {
        super.willInvokeUnit(unit, start);

        int queueSize = _queue.size();
        synchronized (this) {
            // keep track of the largest queue size we've seen
            if (queueSize > _maxQueueSize) {
                _maxQueueSize = queueSize;
            }

            // note the currently invoking unit
            _currentUnit = unit;
            _currentUnitStart = start;
        }
    }

    @Override // from Invoker
    protected void didInvokeUnit (Unit unit, long start)
    {
        super.didInvokeUnit(unit, start);

        synchronized (this) {
            // clear out our currently invoking unit
            _currentUnit = null;
            _currentUnitStart = 0L;
        }
    }

    /** The largest queue size since our last report. */
    protected long _maxQueueSize;

    /** Records the currently invoking unit. */
    protected Object _currentUnit;

    /** The time at which our current unit started. */
    protected long _currentUnitStart;
}
