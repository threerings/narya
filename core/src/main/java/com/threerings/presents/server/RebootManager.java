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

import java.util.Calendar;

import com.samskivert.util.Calendars;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

import com.threerings.util.MessageBundle;

import com.threerings.presents.dobj.RootDObjectManager;

import static com.threerings.presents.Log.log;

/**
 * Handles scheduling and execution of automated server reboots. Note that this service simply
 * shuts down the server and assumes it will be automatically restarted by some external entity. It
 * is generally useful to run a server from a script that automatically restarts it when it
 * terminates.
 */
public abstract class RebootManager
{
    /**
     * An interface for receiving notifications about pending automated shutdowns.
     */
    public static interface PendingShutdownObserver
    {
        /**
         * Called prior to an automated shutdown. NOTE that the shutdown is not guaranteed to
         * happen, this interface is merely for notification purposes.
         *
         * @param warningsLeft The number of warnings left prior to the shutdown. This value will
         * be 0 on the last warning, usually 1-2 minutes prior to the actual shutdown.
         * @param msLeft the approximate number of milliseconds left prior to the shutdown.
         */
        void shutdownPlanned (int warningsLeft, long msLeft);
    }

    /** The default warning times. */
    public static final int[] DEFAULT_WARNINGS = { 30, 20, 15, 10, 5, 2 };

    /**
     * Finishes initialization of the manager.
     */
    public void init ()
    {
        scheduleRegularReboot();
    }

    /**
     * Schedules our next regularly scheduled reboot.
     *
     * @return true if a reboot was scheduled, false if regularly scheduled reboots are disabled.
     */
    public boolean scheduleRegularReboot ()
    {
        // maybe schedule an automatic reboot based on our configuration
        int freq = getDayFrequency();
        int hour = getRebootHour();
        if (freq <= 0) {
            return false;
        }

        Calendars.Builder cal = Calendars.now();
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        cal = cal.zeroTime().addHours(hour).addDays((curHour < hour) ? (freq - 1) : freq);

        // maybe avoid weekends
        if (getSkipWeekends()) {
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                cal.addDays(2);
                break;

            case Calendar.SUNDAY:
                cal.addDays(1);
                break;
            }
        }

        scheduleReboot(cal.toTime(), true, AUTOMATIC_INITIATOR); // schedule exactly
        return true;
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
     * Cancel the currently scheduled reboot, if any.
     */
    public void cancelReboot ()
    {
        if (_interval != null) {
            _interval.cancel();
            _interval = null;
        }
        _nextReboot = 0L;
        _rebootSoon = false;
        _initiator = null;
    }

    /**
     * Schedules a reboot for the specified time.
     */
    public void scheduleReboot (long rebootTime, String initiator)
    {
        scheduleReboot(rebootTime, false, initiator); // inexact: round up to the next warning
    }

    /**
     * Called by an entity that would like to prevent a reboot.
     */
    public int preventReboot (String whereFrom)
    {
        if (whereFrom == null) {
            throw new IllegalArgumentException("whereFrom must be descriptive.");
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
            throw new IllegalArgumentException("no such lockId (" + lockId + ")");
        }
    }

    /**
     * Returns the minutes at which we give warnings. The last value is also the minimum time at
     * which we can possibly reboot after the value of the nextReboot field is changed, to prevent
     * accidentally causing instant server reboots.
     */
    public int[] getWarnings ()
    {
        return DEFAULT_WARNINGS;
    }

    /**
     * Provides us with our dependencies.
     */
    protected RebootManager (PresentsServer server, RootDObjectManager omgr)
    {
        _server = server;
        _omgr = omgr;
    }

    /**
     * Schedule a reboot.
     *
     * @param exact if false we round up to the next highest warning time.
     */
    protected void scheduleReboot (long rebootTime, boolean exact, String initiator)
    {
        // if there's already a reboot scheduled, cancel it
        cancelReboot();

        long now = System.currentTimeMillis();
        rebootTime = Math.max(rebootTime, now); // don't let reboots happen in the past
        long delay = rebootTime - now;
        int[] warnings = getWarnings();
        int level = -1; // assume we're not yet warning
        for (int ii = warnings.length - 1; ii >= 0; ii--) {
            long warnTime = warnings[ii] * (60L * 1000L);
            if (delay <= warnTime) {
                level = ii;
                if (!exact) {
                    rebootTime = now + warnTime;
                }
                break;
            }
        }

        // note our new reboot time and its initiator
        _nextReboot = rebootTime;
        _initiator = initiator;

        doWarning(level, (int)((rebootTime - now) / (60L * 1000L)));
    }

    /**
     * Broadcasts a message to everyone on the server. The following messages will be broadcast:
     * <ul><li> m.rebooting_now
     *     <li> m.reboot_warning (minutes) (message or m.reboot_msg_standard)
     *     <li> m.reboot_delayed
     * </ul>
     */
    protected abstract void broadcast (String message);

    /**
     * Returns the frequency in days of our automatic reboots, or -1 to disable automatically
     * scheduled reboots.
     */
    protected abstract int getDayFrequency ();

    /**
     * Returns the desired hour at which to perform our reboot.
     */
    protected abstract int getRebootHour ();

    /**
     * Returns true if the reboot manager should avoid scheduling automated reboots on the
     * weekends.
     */
    protected abstract boolean getSkipWeekends ();

    /**
     * Returns a custom message to be used when broadcasting a pending reboot.
     */
    protected abstract String getCustomRebootMessage ();

    /**
     * Composes the given reboot message with the minutes and either the custom or standard details
     * about the pending reboot.
     */
    protected String getRebootMessage (String key, int minutes)
    {
        String msg = getCustomRebootMessage();
        if (StringUtil.isBlank(msg)) {
            msg = "m.reboot_msg_standard";
        }

        return MessageBundle.compose(key, MessageBundle.taint("" + minutes), msg);
    }

    /**
     * Do a warning, schedule the next.
     */
    protected void doWarning (int level, int minutes)
    {
        _rebootSoon = (level > -1);

        int[] warnings = getWarnings();
        if (level == warnings.length) {
            if (checkLocks()) {
                return;
            }

            // that's it! do the reboot
            log.info("Performing automatic server reboot/shutdown, as scheduled by: " + _initiator);
            broadcast("m.rebooting_now");

            // wait 1 second, then do it
            new Interval(Interval.RUN_DIRECT) {
                @Override public void expired () {
                    _server.queueShutdown(); // this posts a LongRunnable
                }
            }.schedule(1000);
            return;
        }

        // issue the warning
        if (_rebootSoon) {
            notifyObservers(level);
            broadcast(getRebootMessage("m.reboot_warning", minutes));
        }

        // schedule the next warning
        final int nextLevel = level + 1;
        final int nextMinutes = (nextLevel == warnings.length) ? 0 : warnings[nextLevel];
        _interval = _omgr.newInterval(new Runnable() {
            public void run () {
                doWarning(nextLevel, nextMinutes);
            }
        });
        _interval.schedule(
            Math.max(0L, _nextReboot - (nextMinutes * 60L * 1000L) - System.currentTimeMillis()));
    }

    /**
     * Check to see if there are outstanding reboot locks that may delay the reboot, returning
     * false if there are none.
     */
    protected boolean checkLocks ()
    {
        if (_rebootLocks.isEmpty()) {
            return false;
        }

        log.info("Reboot delayed due to outstanding locks", "locks", _rebootLocks.elements());
        broadcast("m.reboot_delayed");
        (_interval = _omgr.newInterval(new Runnable() {
            public void run () {
                doWarning(getWarnings().length, 0);
            }
        })).schedule(60 * 1000);
        return true;
    }

    /**
     * Notify all PendingShutdownObservers of the pending shutdown!
     */
    protected void notifyObservers (int level)
    {
        final int warningsLeft = getWarnings().length - level - 1;
        final long msLeft = _nextReboot - System.currentTimeMillis();
        _observers.apply(new ObserverList.ObserverOp<PendingShutdownObserver>() {
            public boolean apply (PendingShutdownObserver observer) {
                observer.shutdownPlanned(warningsLeft, msLeft);
                return true;
            }
        });
    }

    /** The server that we're going to reboot. */
    protected PresentsServer _server;

    /** Our distributed object manager. */
    protected RootDObjectManager _omgr;

    /** The time at which our next reboot is scheduled or 0L. */
    protected long _nextReboot;

    /** The entity that scheduled the reboot. */
    protected String _initiator;

    /** True if the reboot is coming soon, within the earliest warning. */
    protected boolean _rebootSoon = false;

    /** The interval scheduled to perform the next step the reboot process. */
    protected Interval _interval;

    /** A list of PendingShutdownObservers. */
    protected ObserverList<PendingShutdownObserver> _observers = ObserverList.newFastUnsafe();

    /** The next reboot lock id. */
    protected int _nextRebootLockId = 0;

    /** Things that can delay the reboot. */
    protected HashIntMap<String> _rebootLocks = new HashIntMap<String>();

    protected static final String AUTOMATIC_INITIATOR = "automatic";
}
