//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import java.util.Calendar;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

import com.threerings.util.MessageBundle;

import static com.threerings.presents.Log.log;

/**
 * Handles scheduling and execution of automated server reboots. Note that this
 * service simply shuts down the server and assumes it will be automatically
 * restarted by some external entity. It is generally useful to run a server
 * from a script that automatically restarts it when it terminates.
 */
public abstract class RebootManager
{
    /**
     * An interface for receiving notifications about pending automated
     * shutdowns.
     */
    public static interface PendingShutdownObserver
    {
        /**
         * Called prior to an automated shutdown. NOTE that the shutdown
         * is not guaranteed to happen, this interface is merely for
         * notification purposes.
         *
         * @param warningsLeft The number of warnings left prior to the
         * shutdown. This value will be 0 on the last warning, usually 1-2
         * minutes prior to the actual shutdown.
         * @param msLeft the approximate number of milliseconds left prior to
         * the shutdown.
         */
        public void shutdownPlanned (int warningsLeft, long msLeft);
    }

    /**
     * Creates a reboot manager that will shutdown the supplied server when the
     * time comes.
     */
    public RebootManager (PresentsServer server)
    {
        _server = server;
    }

    /**
     * Finish initialization of the manager.
     */
    public void init ()
    {
        // maybe schedule an automatic reboot based on our configuration
        int freq = getDayFrequency();
        if (freq == -1) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, freq);
        cal.set(Calendar.HOUR_OF_DAY, getRebootHour());
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // maybe avoid weekends
        if (getSkipWeekends()) {
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            switch (dow) {
            case Calendar.SATURDAY:
                if (freq > 1) {
                    cal.add(Calendar.DATE, -1);
                }
                break;

            case Calendar.SUNDAY:
                if (freq > 2) {
                    cal.add(Calendar.DATE, -2);
                }
                break;
            }
        }

        scheduleReboot(cal.getTimeInMillis(), "automatic");
    }

    /**
     * Is the manager planning a shutdown in the near future?
     */
    public boolean willShutdownSoon ()
    {
        return _rebootSoon;
    }

    /**
     * Add an observer to the observer list.
     */
    public void addObserver (PendingShutdownObserver observer)
    {
        _observers.add(observer);
    }

    /**
     * Schedules a reboot for the specified time.
     */
    public void scheduleReboot (long rebootTime, String initiator)
    {
        // if there's already a reboot scheduled, cancel it
        if (_interval != null) {
            _interval.cancel();
            _interval = null;
        }

        // note our new reboot time and its initiator
        _nextReboot = rebootTime;
        _initiator = initiator;

        // see if the reboot is happening within the time specified by the
        // longest warning; if so, issue the appropriate warning
        long now = System.currentTimeMillis();
        for (int ii = WARNINGS.length - 1; ii >= 0; ii--) {
            long warnTime = WARNINGS[ii] * 60 * 1000;
            if (now + warnTime >= _nextReboot) {
                doWarning(ii);
                return;
            }
        }

        // otherwise, it's further off; schedule an interval to wake up when we
        // should issue the first pre-reboot warning
        _rebootSoon = false;
        long firstWarnTime = (_nextReboot - (WARNINGS[0] * 60 * 1000)) - now;
        _interval = new Interval(PresentsServer.omgr) {
            public void expired () {
                doWarning(0);
            }
        };
        _interval.schedule(firstWarnTime);
    }

    /**
     * Called by an entity that would like to prevent a reboot.
     */
    public int preventReboot (String whereFrom)
    {
        if (whereFrom == null) {
            throw new IllegalArgumentException(
                "whereFrom must be descriptive.");
        }
        int lockId = _nextRebootLockId++;
        _rebootLocks.put(lockId, whereFrom);
        return lockId;
    }

    /**
     * Release a reboot lock.
     */
    public void allowReboot (int lockId)
    {
        if (null == _rebootLocks.remove(lockId)) {
            throw new IllegalArgumentException(
                "no such lockId (" + lockId + ")");
        }
    }

    /**
     * Broadcasts a message to everyone on the server. The following
     * messages will be broadcast:
     * <ul><li> m.rebooting_now
     *     <li> m.reboot_warning (minutes) (message or m.reboot_msg_standard)
     *     <li> m.reboot_delayed
     * </ul>
     */
    protected abstract void broadcast (String message);

    /**
     * Returns the frequency in days of our automatic reboots, or -1 to disable
     * automatically scheduled reboots.
     */
    protected abstract int getDayFrequency ();

    /**
     * Returns the desired hour at which to perform our reboot.
     */
    protected abstract int getRebootHour ();

    /**
     * Returns true if the reboot manager should avoid scheduling automated
     * reboots on the weekends.
     */
    protected abstract boolean getSkipWeekends ();

    /**
     * Returns a custom message to be used when broadcasting a pending reboot.
     */
    protected abstract String getCustomRebootMessage ();

    /**
     * Do a warning, schedule the next.
     */
    protected void doWarning (final int level)
    {
        _rebootSoon = true;
        if (level == WARNINGS.length) {
            if (checkLocks()) {
                return;
            }

            // that's it! do the reboot
            log.info("Performing automatic server reboot/shutdown, " +
                     "as scheduled by: " + _initiator);
            broadcast("m.rebooting_now");

            // wait 1 second, then do it
            new Interval() { // Note: This interval does not run on the dobj thread
                public void expired () {
                    // ...but we then post a LongRunnable...
                    PresentsServer.omgr.postRunnable(new PresentsDObjectMgr.LongRunnable() {
                        public void run () {
                            _server.shutdown();
                        }
                    });
                }
            }.schedule(1000);
            return;
        }

        // issue the warning
        String msg = getCustomRebootMessage();
        if (StringUtil.isBlank(msg)) {
            msg = "m.reboot_msg_standard";
        }
        int minutes = WARNINGS[level];
        msg = MessageBundle.compose(
            "m.reboot_warning", MessageBundle.taint("" + minutes), msg);
        broadcast(msg);
        if (level < WARNINGS.length - 1) {
            minutes -= WARNINGS[level + 1];
        }

        // schedule the next warning
        _interval = new Interval(PresentsServer.omgr) {
            public void expired () {
                doWarning(level + 1);
            }
        };
        _interval.schedule(minutes * 60 * 1000);
        notifyObservers(level);
    }

    /**
     * Check to see if there are outstanding reboot locks that may delay the
     * reboot, returning false if there are none.
     */
    protected boolean checkLocks ()
    {
        if (_rebootLocks.isEmpty()) {
            return false;
        }

        log.info("Reboot delayed due to outstanding locks: " +
                 StringUtil.toString(_rebootLocks.elements()));
        broadcast("m.reboot_delayed");
        _interval = new Interval(PresentsServer.omgr) {
            public void expired () {
                doWarning(WARNINGS.length);
            }
        };
        _interval.schedule(60 * 1000);
        return true;
    }

    /**
     * Notify all PendingShutdownObservers of the pending shutdown!
     */
    protected void notifyObservers (int level)
    {
        final int warningsLeft = WARNINGS.length - level - 1;
        final long msLeft = 1000L * 60L * WARNINGS[level];
        _observers.apply(
            new ObserverList.ObserverOp<PendingShutdownObserver>() {
            public boolean apply (PendingShutdownObserver observer) {
                observer.shutdownPlanned(warningsLeft, msLeft);
                return true;
            }
        });
    }

    /** The server we will reboot. */
    protected PresentsServer _server;

    /** The time at which our next reboot is scheduled or 0L. */
    protected long _nextReboot;

    /** The entity that scheduled the reboot. */
    protected String _initiator;

    /** True if the reboot is coming soon, within the earliest warning. */
    protected boolean _rebootSoon = false;

    /** The interval scheduled to perform the next step the reboot process. */
    protected Interval _interval;

    /** A list of PendingShutdownObservers. */
    protected ObserverList<PendingShutdownObserver> _observers =
        new ObserverList<PendingShutdownObserver>(
            ObserverList.FAST_UNSAFE_NOTIFY);

    /** The next reboot lock id. */
    protected int _nextRebootLockId = 0;

    /** Things that can delay the reboot. */
    protected HashIntMap<String> _rebootLocks = new HashIntMap<String>();

    /** The minutes at which we give warnings. The last value is also the
     * minimum time at which we can possibly reboot after the value of the
     * nextReboot field is changed, to prevent accidentally causing instant
     * server reboots. */
    public static final int[] WARNINGS = { 30, 20, 15, 10, 5, 2 };
}
