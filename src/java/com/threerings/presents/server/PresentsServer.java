//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import java.util.ArrayList;

import com.samskivert.util.Interval;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;
import com.samskivert.util.SystemInfo;

import com.threerings.util.signal.SignalManager;

import com.threerings.presents.Log;
import com.threerings.presents.client.Client;
import com.threerings.presents.server.net.ConnectionManager;

/**
 * The presents server provides a central point of access to the various
 * facilities that make up the presents framework. To facilitate extension
 * and customization, a single instance of the presents server should be
 * created and initialized in a process. To facilitate easy access to the
 * services provided by the presents server, static references to the
 * various managers are made available in the <code>PresentsServer</code>
 * class. These will be configured when the singleton instance is
 * initialized.
 */
public class PresentsServer
    implements SignalManager.SignalHandler
{
    /** Used to generate "state of the server" reports. See {@link
     * #registerReporter}. */
    public static interface Reporter
    {
        /**
         * Requests that this reporter append its report to the supplied
         * string buffer.
         *
         * @param buffer the string buffer to which the report text should
         * be appended.
         * @param now the time at which the report generation began, in
         * epoch millis.
         * @param sinceLast number of milliseconds since the last time we
         * generated a report.
         */
        public void appendReport (
            StringBuffer buffer, long now, long sinceLast);
    }

    /** Implementers of this interface will be notified when the server is
     * shutting down. */
    public static interface Shutdowner
    {
        /**
         * Called when the server is shutting down.
         */
        public void shutdown ();
    }

    /** The manager of network connections. */
    public static ConnectionManager conmgr;

    /** The manager of clients. */
    public static ClientManager clmgr;

    /** The distributed object manager. */
    public static PresentsDObjectMgr omgr;

    /** The invocation manager. */
    public static InvocationManager invmgr;

    /** This is used to invoke background tasks that should not be allowed
     * to tie up the distributed object manager thread. This is generally
     * used to talk to databases and other (relatively) slow entities. */
    public static PresentsInvoker invoker;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // output general system information
        SystemInfo si = new SystemInfo();
        Log.info("Starting up server [os=" + si.osToString() +
                 ", jvm=" + si.jvmToString() +
                 ", mem=" + si.memoryToString() + "].");

        // register a ctrl-c handler
        SignalManager.registerSignalHandler(SignalManager.SIGINT, this);

        // create our list of shutdowners
        _downers = new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

        // create our distributed object manager
        omgr = createDObjectManager();

        // create and start up our invoker
        invoker = new PresentsInvoker(omgr);
        invoker.start();

        // create our connection manager
        conmgr = new ConnectionManager(getListenPorts());
        conmgr.setAuthenticator(new DummyAuthenticator());

        // create our client manager
        clmgr = new ClientManager(conmgr);

        // create our invocation manager
        invmgr = new InvocationManager(omgr);

        // initialize the time base services
        TimeBaseProvider.init(invmgr, omgr);

        // queue up an interval which will generate reports
        new Interval(omgr) {
            public void expired () {
                generateReport(System.currentTimeMillis());
            }
        }.schedule(REPORT_INTERVAL, true);
    }

    /**
     * Creates the distributed object manager to be used on this server.
     */
    protected PresentsDObjectMgr createDObjectManager ()
    {
        return new PresentsDObjectMgr();
    }

    /**
     * Returns the port on which the connection manager will listen for
     * client connections.
     */
    protected int[] getListenPorts ()
    {
        return new int[] { Client.DEFAULT_SERVER_PORT };
    }

    /**
     * Starts up all of the server services and enters the main server
     * event loop.
     */
    public void run ()
    {
        // post a unit that will start up the connection manager when
        // everything else in the dobjmgr queue is processed
        omgr.postRunnable(new Runnable() {
            public void run () {
                // start up the connection manager
                conmgr.start();
            }
        });
        // invoke the dobjmgr event loop
        omgr.run();
    }

    // documentation inherited from interface
    public boolean signalReceived (int signo)
    {
        // this is called when we receive a ctrl-c
        Log.info("Shutdown initiated by received signal (" + signo + ")");
        queueShutdown();
        return true;
    }

    /**
     * A report is generated by the presents server periodically in which
     * server entities can participate by registering a {@link Reporter}
     * with this method.
     */
    public static void registerReporter (Reporter reporter)
    {
        _reporters.add(reporter);
    }

    /**
     * Generates and logs a "state of server" report.
     */
    protected void generateReport (long now)
    {
        long sinceLast = now - _lastReportStamp;
        long uptime = now - _serverStartTime;
        StringBuffer report = new StringBuffer("State of server report:\n");

        report.append("- Uptime: ");
        report.append(StringUtil.intervalToString(uptime)).append(", ");
        report.append(StringUtil.intervalToString(sinceLast));
        report.append(" since last report\n");

        // report on the state of memory
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory(), max = rt.maxMemory();
        long used = (total - rt.freeMemory());
        report.append("- Memory: ").append(used/1024).append("k used, ");
        report.append(total/1024).append("k total, ");
        report.append(max/1024).append("k max\n");

        for (int ii = 0; ii < _reporters.size(); ii++) {
            Reporter rptr = (Reporter)_reporters.get(ii);
            try {
                rptr.appendReport(report, now, sinceLast);
            } catch (Throwable t) {
                Log.warning("Reporter choked [rptr=" + rptr + "].");
                Log.logStackTrace(t);
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
        if (report.charAt(blen-1) == '\n') {
            report.delete(blen-1, blen);
        }

        _lastReportStamp = now;
        logReport(report.toString());
    }

    /**
     * Logs the state of the server report via the default logging
     * mechanism. Derived classes may wish to log the state of the server
     * report via a different means.
     */
    protected void logReport (String report)
    {
        Log.info(report);
    }

    /**
     * Requests that the server shut down. All registered shutdown
     * participants will be shut down, following which the server process
     * will be terminated.
     */
    public void shutdown ()
    {
        ObserverList downers = _downers;
        if (downers == null) {
            Log.warning("Refusing repeat shutdown request.");
            return;
        }
        _downers = null;

        // shut down the connection manager (this will cease all network
        // activity but not actually close the connections)
        if (conmgr.isRunning()) {
            conmgr.shutdown();
        }

        // shut down all shutdown participants
        downers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((Shutdowner)observer).shutdown();
                return true;
            }
        });

        // finally shut down the invoker and distributed object manager
        // (The invoker does both for us.)
        invoker.shutdown();
    }

    /**
     * Queues up a request to shutdown on the dobjmgr thread. This method
     * may be safely called from any thread.
     */
    public void queueShutdown ()
    {
        omgr.postRunnable(new Runnable() {
            public void run () {
                shutdown();
            }
        });
    }

    /**
     * Registers an entity that will be notified when the server is
     * shutting down.
     */
    public static void registerShutdowner (Shutdowner downer)
    {
        _downers.add(downer);
    }

    public static void main (String[] args)
    {
        Log.info("Presents server starting...");

        PresentsServer server = new PresentsServer();
        try {
            // initialize the server
            server.init();

            // check to see if we should load and invoke a test module
            // before running the server
            String testmod = System.getProperty("test_module");
            if (testmod != null) {
                try {
                    Log.info("Invoking test module [mod=" + testmod + "].");
                    Class tmclass = Class.forName(testmod);
                    Runnable trun = (Runnable)tmclass.newInstance();
                    trun.run();
                } catch (Exception e) {
                    Log.warning("Unable to invoke test module " +
                                "[mod=" + testmod + "].");
                    Log.logStackTrace(e);
                }
            }

            // start the server to running (this method call won't return
            // until the server is shut down)
            server.run();

        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
            System.exit(-1);
        }
    }

    /** The time at which the server was started. */
    protected long _serverStartTime = System.currentTimeMillis();

    /** The last time at which {@link #generateReport} was run. */
    protected long _lastReportStamp = _serverStartTime;

    /** Used to generate "state of server" reports. */
    protected static ArrayList _reporters = new ArrayList();

    /** A list of shutdown participants. */
    protected static ObserverList _downers;;

    /** The frequency with which we generate "state of server" reports. */
    protected static final long REPORT_INTERVAL = 15 * 60 * 1000L;
}
