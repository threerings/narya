//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.server;

import java.util.concurrent.Executor;

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
     * if profiling is enabled ({@link Invoker#PERF_TRACK}).
     */
    public ReportingInvoker (String name, Executor receiver, ReportManager repmgr)
    {
        super(name, receiver);
        if (PERF_TRACK) {
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

    @Override // from Invoker
    protected void willInvokeUnit (Unit unit, long start)
    {
        super.willInvokeUnit(unit, start);

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

        synchronized (this) {
            // clear out our currently invoking unit
            _currentUnit = null;
            _currentUnitStart = 0L;
        }
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

    /** Used to track runtime statistics. */
    protected Stats _recent = new Stats(), _current = _recent;

    /** The total number of units run. */
    protected int _totalUnitsRun;

    /** Records the currently invoking unit. */
    protected Object _currentUnit;

    /** The time at which our current unit started. */
    protected long _currentUnitStart;
}
