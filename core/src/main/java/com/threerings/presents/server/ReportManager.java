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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.RootDObjectManager;

import static com.threerings.presents.Log.log;

/**
 * Handles the generation of server status reports.
 */
@Singleton
public class ReportManager
{
    /**
     * Used to generate "state of the server" reports.
     * See {@link ReportManager#registerReporter(Reporter)}.
     * */
    public static interface Reporter
    {
        /**
         * Requests that this reporter append its report to the supplied string buffer.
         *
         * @param buffer the string buffer to which the report text should be appended.
         * @param now the time at which the report generation began, in epoch millis.
         * @param sinceLast number of milliseconds since the last time we generated a report.
         * @param reset if true, all accumulating stats should be reset, if false they should be
         * allowed to continue to accumulate.
         */
        void appendReport (StringBuilder buffer, long now, long sinceLast, boolean reset);
    }

    /** A string constant representing the default report. */
    public static final String DEFAULT_TYPE = "";

    /** A string constant representing a report with detailed profiling information. */
    public static final String PROFILE_TYPE = "profile";

    /**
     * Starts up our periodic report generation task.
     */
    public void activatePeriodicReport (RootDObjectManager omgr)
    {
        // queue up an interval which will generate reports as long as the omgr is alive
        omgr.newInterval(new Runnable() {
            public void run () {
                logReport(LOG_REPORT_HEADER +
                          generateReport(DEFAULT_TYPE, System.currentTimeMillis(), true));
            }
        }).schedule(getReportInterval(), true);
    }

    /**
     * Registers a reporter for the default state of server report.
     */
    public void registerReporter (Reporter reporter)
    {
        registerReporter(DEFAULT_TYPE, reporter);
    }

    /**
     * Registers a reporter for the report of the specified type.
     */
    public void registerReporter (String type, Reporter reporter)
    {
        _reporters.put(type, reporter);
    }

    /**
     * Generate a default state of server report.
     */
    public String generateReport ()
    {
        return generateReport(DEFAULT_TYPE);
    }

    /**
     * Generates a report for all system services registered as a {@link Reporter}.
     */
    public String generateReport (String type)
    {
        return generateReport(type, System.currentTimeMillis(), false);
    }

    /**
     * Generates and logs a "state of server" report.
     */
    protected String generateReport (String type, long now, boolean reset)
    {
        long sinceLast = now - _lastReportStamp;
        long uptime = now - _serverStartTime;
        StringBuilder report = new StringBuilder();

        // add standard bits to the default report
        if (DEFAULT_TYPE.equals(type)) {
            report.append("- Uptime: ");
            report.append(StringUtil.intervalToString(uptime)).append("\n");
            report.append("- Report period: ");
            report.append(StringUtil.intervalToString(sinceLast)).append("\n");

            // report on the state of memory
            Runtime rt = Runtime.getRuntime();
            long total = rt.totalMemory(), max = rt.maxMemory();
            long used = (total - rt.freeMemory());
            report.append("- Memory: ").append(used/1024).append("k used, ");
            report.append(total/1024).append("k total, ");
            report.append(max/1024).append("k max\n");
        }

        for (Reporter rptr : _reporters.get(type)) {
            try {
                rptr.appendReport(report, now, sinceLast, reset);
            } catch (Throwable t) {
                log.warning("Reporter choked", "rptr", rptr, t);
            }
        }

        /* The following Interval debug methods are no longer supported,
         * but they could be added back easily if needed.
        report.append("* samskivert.Interval:\n");
        report.append("- Registered intervals: ");
        report.append(Interval.registeredIntervalCount());
        report.append("\n- Fired since last report: ");
        report.append(Interval.getAndClearFiredIntervals());
        report.append("\n");
        */

        // strip off the final newline
        int blen = report.length();
        if (report.length() > 0 && report.charAt(blen-1) == '\n') {
            report.delete(blen-1, blen);
        }

        // only reset the last report time if this is a periodic report
        if (reset) {
            _lastReportStamp = now;
        }

        return report.toString();
    }

    /**
     * Logs the state of the server report via the default logging mechanism.  Derived classes may
     * wish to log the state of the server report via a different means.
     */
    protected void logReport (String report)
    {
        log.info(report);
    }

    /**
     * Returns the period on which to schedule our report generation.
     */
    protected long getReportInterval ()
    {
        return REPORT_INTERVAL;
    }

    /** The time at which the server was started. */
    protected long _serverStartTime = System.currentTimeMillis();

    /** The last time at which {@link #generateReport(String,long,boolean)} was run. */
    protected long _lastReportStamp = _serverStartTime;

    /** Used to generate "state of server" reports. */
    protected Multimap<String, Reporter> _reporters = ArrayListMultimap.create();

    /** The frequency with which we generate "state of server" reports. */
    protected static final long REPORT_INTERVAL = 15 * 60 * 1000L;

    /** The header to prefix to our logged reports. */
    protected static final String LOG_REPORT_HEADER = "State of server report:\n";
}
