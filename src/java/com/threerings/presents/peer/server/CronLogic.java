//
// $Id$

package com.threerings.presents.peer.server;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Interval;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.RandomUtil;
import com.samskivert.util.ResultListener;

import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.server.PresentsDObjectMgr;

import static com.threerings.presents.Log.log;

/**
 * Provides peer-aware cron services. Jobs are scheduled at fixed wall times and the peers
 * coordinate to ensure that only one peer runs a job at its scheduled wall time.
 *
 * <p>Note: these jobs <em>do not</em> run on the dobj thread, they run on individual separate
 * worker threads. If such coordination is needed with the dobj world, it must be handled manually
 * by the job.
 */
@Singleton
public class CronLogic
{
    @Inject public CronLogic (Lifecycle cycle, PresentsDObjectMgr omgr)
    {
        _ticker = new JobTicker(omgr);
        cycle.addComponent(new Lifecycle.Component() {
            public void init () {
                // scheule our ticker to start running at 0 milliseconds after the minute; we'll
                // randomize from there, but this reduces any initial bias
                Calendar cal = Calendar.getInstance();
                long curmils = cal.get(Calendar.SECOND) * 1000L + cal.get(Calendar.MILLISECOND);
                _ticker.schedule(60 * 1000L - curmils);
            }
            public void shutdown () {
                _ticker.cancel();
            }
        });
    }

    /**
     * Schedules a job every N hours. We schedule the job at midnight and then again every N hours
     * for the rest of the day. This means that if N does not evenly divide 24, there may be a gap
     * smaller than N between the last job of the day and 00:00 of the next day.
     *
     * <p> Note that the job will be scheduled to run at some arbitrary minute after the our based
     * on the hashcode of the name of the job class.
     *
     * @param job a runnable that will be executed periodically <em>on a separate thread</em>.
     * @param hourlyPeriod the number of hours between executions of this job.
     */
    public void scheduleEvery (int hourlyPeriod, Runnable job)
    {
        int minOfHour = job.getClass().toString().hashCode() % 60;
        int minOfDay = 0;
        synchronized (_jobs) {
            while (minOfDay < 24*60) {
                _jobs.put(minOfDay + minOfHour, job);
                minOfDay += hourlyPeriod * 60;
            }
        }
    }

    /**
     * Schedules a job to run once per day at the specified hour.
     *
     * <p> Note that the job will be scheduled to run at some arbitrary minute after the our based
     * on the hashcode of the name of the job class.
     *
     * @param job a runnable that will be executed periodically <em>on a separate thread</em>.
     * @param hour the hour of the day at which to execute this job.
     */
    public void scheduleAt (int hour, Runnable job)
    {
        int minOfHour = job.getClass().toString().hashCode() % 60;
        synchronized (_jobs) {
            _jobs.put(hour * 60 + minOfHour, job);
        }
    }

    /**
     * Removes the specified job from the schedule.
     */
    public void unschedule (Runnable job)
    {
        synchronized (_jobs) {
            // we have to iterate over our entire jobs table to remove all occurrances of this job,
            // but the table's not huge and we don't do this very often so it's no big deal
            for (Iterator<Map.Entry<Integer, Runnable>> iter = _jobs.entries().iterator();
                 iter.hasNext(); ) {
                Map.Entry<Integer, Runnable> entry = iter.next();
                if (entry.getValue() == job) {
                    iter.remove();
                }
            }
        }
    }

    protected void executeJobs (int minuteOfDay)
    {
        List<Runnable> jobs = Lists.newArrayList();
        synchronized (_jobs) {
            List<Runnable> sched = _jobs.get(minuteOfDay);
            if (sched != null) {
                jobs.addAll(sched);
            }
        }
        for (Runnable job : jobs) {
            executeJob(job);
        }
    }

    protected void executeJob (final Runnable job)
    {
        final NodeObject.Lock lock = new NodeObject.Lock(CRON_LOCK, job.getClass().getName());
        _peerMan.acquireLock(lock, new ResultListener<String>() {
            public void requestCompleted (String result) {
                if (!result.equals(_peerMan.getNodeObject().nodeName)) {
                    return;
                }
                startJob(job, lock);
            }
            public void requestFailed (Exception cause) {
                log.warning("Failed to acquire lock for job", "job", job, cause);
            }
        });
    }

    protected void startJob (final Runnable job, final NodeObject.Lock lock)
    {
        if (_running.putIfAbsent(job.getClass(), lock) != null) {
            log.info("Dropping job as it is still executing", "job", job);
            return;
        }
        new Thread() {
            public void run () {
                try {
                    job.run();
                } catch (Throwable t) {
                    log.warning("Job failed", "job", job, t);
                } finally {
                    jobCompleted(job);
                }
            }
        }.start();
    }

    protected void jobCompleted (Runnable job)
    {
        final NodeObject.Lock lock = _running.remove(job.getClass());
        if (lock != null) {
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    _peerMan.releaseLock(lock, new ResultListener.NOOP<String>());
                }
            });
        }
    }

    protected class JobTicker extends Interval
    {
        public JobTicker (PresentsDObjectMgr omgr) {
            super(omgr);
        }

        @Override public void expired () {
            // if the current minute is less than our previous, we wrapped around midnight
            int curMinute = getMinuteOfDay();
            if (curMinute < _prevMinute) {
                processMinutes(_prevMinute+1, 24*60-1);
                processMinutes(0, curMinute);
            } else {
                processMinutes(_prevMinute+1, curMinute);
            }

            // note our previously executed minute
            _prevMinute = curMinute;

            // schedule ourselves for 60000 +/- rand(1000) millis in the future to randomize which
            // node is likely to win the job lottery every minute
            schedule(61*1000L - RandomUtil.getInt(2000));
        }

        protected int getMinuteOfDay () {
            _cal.setTimeInMillis(System.currentTimeMillis());
            return _cal.get(Calendar.HOUR_OF_DAY) * 60 + _cal.get(Calendar.MINUTE);
        }

        protected void processMinutes (int fromMinute, int toMinute) {
            for (int mm = fromMinute; mm <= toMinute; mm++) {
                executeJobs(mm);
            }
        }

        protected Calendar _cal = Calendar.getInstance();
        protected int _prevMinute = getMinuteOfDay();
    }

    /** The ticker that handles our periodic jobs. */
    protected JobTicker _ticker;

    /** A map of all jobs scheduled for a single day. */
    protected ListMultimap<Integer, Runnable> _jobs = ArrayListMultimap.create();

    /** A map of jobs currently running on this node. */
    protected ConcurrentHashMap<Class<? extends Runnable>, NodeObject.Lock> _running =
        new ConcurrentHashMap<Class<? extends Runnable>, NodeObject.Lock>();

    // los dependidos
    @Inject protected PeerManager _peerMan;
    @Inject protected PresentsDObjectMgr _omgr;

    /** The name of the locks we use to coordinate cron jobs between peers. */
    protected static final String CRON_LOCK = "peer_cron_job";
}
