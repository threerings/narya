//
// $Id: PresentsServer.java,v 1.33 2003/03/31 02:10:37 mdb Exp $

package com.threerings.presents.server;

import java.util.ArrayList;

import com.samskivert.util.IntervalManager;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;
import com.samskivert.util.SystemInfo;

import com.threerings.util.signal.SignalManager;

import com.threerings.presents.Log;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.server.net.ConnectionManager;
import com.threerings.presents.server.util.SafeInterval;
import com.threerings.presents.util.Invoker;

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
    public static Invoker invoker;

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

        // create our distributed object manager
        omgr = new PresentsDObjectMgr();

        // create and start up our invoker
        invoker = new Invoker(omgr);
        invoker.start();

        // create our connection manager
        conmgr = new ConnectionManager(getListenPort());
        conmgr.setAuthenticator(new DummyAuthenticator());

        // create our client manager
        clmgr = new ClientManager(conmgr);

        // create our invocation manager
        invmgr = new InvocationManager(omgr);

        // initialize the time base services
        TimeBaseProvider.init(invmgr, omgr);

        // queue up an interval which will generate reports
        IntervalManager.register(new SafeInterval(omgr) {
            public void run () {
                generateReport(System.currentTimeMillis());
            }
        }, REPORT_INTERVAL, null, true);
    }

    /**
     * Returns the port on which the connection manager will listen for
     * client connections.
     */
    protected int getListenPort ()
    {
        return Client.DEFAULT_SERVER_PORT;
    }

    /**
     * Starts up all of the server services and enters the main server
     * event loop.
     */
    public void run ()
    {
        // post a unit that will start up the connection manager when
        // everything else in the dobjmgr queue is processed
        omgr.postUnit(new Runnable() {
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
        omgr.postUnit(new Runnable() {
            public void run () {
                shutdown();
            }
        });
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
        // shut down the connection manager (this will cease all network
        // activity but not actually close the connections)
        conmgr.shutdown();

        // shut down all shutdown participants
        _downers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((Shutdowner)observer).shutdown();
                return true;
            }
        });

        // finally shut down the invoker and distributed object manager
        invoker.shutdown();
        omgr.shutdown();
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
    protected static ObserverList _downers =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The frequency with which we generate "state of server" reports. */
    protected static final long REPORT_INTERVAL = 15 * 60 * 1000L;
}
