//
// $Id: Invoker.java,v 1.7 2003/04/10 21:04:55 mdb Exp $

package com.threerings.presents.util;

import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.Histogram;
import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;
import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.presents.server.PresentsServer.Reporter;
import com.threerings.presents.server.PresentsServer;

/**
 * The invoker is used to invoke self-contained units of code on an
 * invoking thread. Each invoker is associated with its own thread and
 * that thread is used to invoke all of the units posted to that invoker
 * in the order in which they were posted. The invoker also provides a
 * convenient mechanism for processing the result of an invocation back on
 * the dobjmgr thread.
 *
 * <p> The invoker is a useful tool for services that need to block and
 * therefore cannot be run on the distributed object thread. For example,
 * a user of the Presents system might provide an invoker on which to run
 * database queries.
 *
 * <p> Bear in mind that each invoker instance runs units on its own
 * thread and care must be taken to ensure that code running on separate
 * invokers properly synchronizes access to shared information. Where
 * possible, complete isolation of the services provided by a particular
 * invoker is desirable.
 */
public class Invoker extends LoopingThread
    implements Reporter
{
    /**
     * The unit encapsulates a unit of executable code that will be run on
     * the invoker thread. It also provides facilities for additional code
     * to be run on the dobjmgr thread once the primary code has completed
     * on the invoker thread.
     */
    public static abstract class Unit implements Runnable
    {
        /**
         * This method is called on the invoker thread and should be used
         * to perform the primary function of the unit. It can return true
         * to cause the <code>handleResult</code> method to be
         * subsequently invoked on the dobjmgr thread (generally to allow
         * the results of the invocation to be acted upon back in the
         * context of the distributed object world) or false to indicate
         * that no further processing should be performed.
         *
         * <p> Note that an invocation unit can do things like post events
         * from the invoker thread (which includes modification of
         * distributed object fields) and need not jump over to the
         * dobjmgr thread to do so. However, it cannot <em>read</em>
         * distributed object fields from the invoker thread. Any field
         * values needed during the course of the invocation should be
         * provided from the dobjmgr thread at the time that the
         * invocation unit is created and posted.
         *
         * @return true if the <code>handleResult</code> method should be
         * invoked on the dobjmgr thread, false if not.
         */
        public abstract boolean invoke ();

        /**
         * Invocation unit implementations can implement this function to
         * perform any post-unit-invocation processing back on the dobjmgr
         * thread. It will be invoked if <code>invoke</code> returns true.
         */
        public void handleResult ()
        {
            // do nothing by default
        }

        // we want to be a runnable to make the dobjmgr happy, but we'd
        // like for invocation unit implementations to be able to put
        // their result handling code into an aptly named method
        public void run ()
        {
            handleResult();
        }
    }

    /**
     * Creates an invoker that will post results to the supplied
     * distributed object manager.
     */
    public Invoker (PresentsDObjectMgr omgr)
    {
        super("presents.Invoker");
        _omgr = omgr;
        if (PERF_TRACK) {
            PresentsServer.registerReporter(this);
        }
    }

    /**
     * Posts a unit to this invoker for subsequent invocation on the
     * invoker's thread.
     */
    public void postUnit (Unit unit)
    {
        // simply append it to the queue
        _queue.append(unit);
    }

    // documentation inherited
    public void iterate ()
    {
        // pop the next item off of the queue
        Object unit = _queue.get();

        // if it's a unit, we invoke it
        if (unit instanceof Runnable) {
            long start;
            if (PERF_TRACK) {
                start = System.currentTimeMillis();
                _unitsRun++;
            }

            try {
                if (((Unit)unit).invoke()) {
                    // if it returned true, we post it to the dobjmgr
                    // thread to invoke the result processing
                    _omgr.postUnit((Runnable)unit);
                }

                // track some performance metrics
                if (PERF_TRACK) {
                    long duration = System.currentTimeMillis() - start;
                    Object key = unit.getClass();

                    Histogram histo = (Histogram)_tracker.get(key);
                    if (histo == null) {
                        // track in buckets of 50ms up to 500ms
                        _tracker.put(key, histo = new Histogram(0, 50, 10));
                    }
                    histo.addValue((int)duration);
                }

            } catch (Exception e) {
                Log.warning("Invocation unit failed [unit=" + unit + "].");
                Log.logStackTrace(e);
            }

        } else {
            // if it's not a runnable, it was just an object posted to our
            // queue to wake us up and tell us to go away
            _running = false;
        }
    }

    // documentation inherited
    public void shutdown ()
    {
        // we do some custom shutdown business: add a non-runnable to the
        // queue which, when the invoker gets to it, will cause it to shut
        // itself down
        _queue.append(new Integer(0));
    }

    // documentation inherited from interface
    public void appendReport (StringBuffer buffer, long now, long sinceLast)
    {
        buffer.append("* presents.util.Invoker:");
        buffer.append("- Units executed: ").append(_unitsRun).append("\n");
        _unitsRun = 0;
        for (Iterator iter = _tracker.keySet().iterator(); iter.hasNext(); ) {
            Class key = (Class)iter.next();
            Histogram histo = (Histogram)_tracker.get(key);
            buffer.append("  ");
            buffer.append(StringUtil.shortClassName(key)).append(": ");
            buffer.append(histo.summarize()).append("\n");
            histo.clear();
        }
    }

    /** The invoker's queue of units to be executed. */
    protected Queue _queue = new Queue();

    /** The object manager with which we're working. */
    protected PresentsDObjectMgr _omgr;

    /** Tracks the counts of invocations by unit's class. */
    protected HashMap _tracker = new HashMap();

    /** The total number of invoker units run since the last report. */
    protected int _unitsRun;

    /** Whether or not to track invoker unit performance. */
    protected static final boolean PERF_TRACK = true;
}
