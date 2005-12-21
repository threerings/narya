package com.threerings.media;

import java.awt.Component;

import com.threerings.media.FrameManager;
import com.threerings.media.FrameParticipant;

public abstract class FrameInterval
    implements FrameParticipant
{
    /**
     * Constructor - registers the interval as a frame participant
     */
    public FrameInterval (FrameManager mgr)
    {
        _mgr = mgr;
    }

    // documentation inhertied from FrameParticipant
    public Component getComponent ()
    {
        return null;
    }

    // documentation inherited from FrameParticipant
    public boolean needsPaint ()
    {
        return false;
    }

    // documentation inherited from FrameParticipant
    public void tick (long tickStamp)
    {
        if (_nextTime == -1) {
            // First time through
            _nextTime = tickStamp + _initDelay;
        } else if (tickStamp >= _nextTime) {

            // If we're repeating, set the next time to run, otherwise, reset
            if (_repeatDelay != 0L) {
                _nextTime += _repeatDelay;
            } else {
                _nextTime = -1;
                cancel();
            }
            expired();
        }
    }

    /**
     *
     * The main method where your interval should do its work.
     *
     */
    public abstract void expired ();

    /**
     * Schedule the interval to execute once, after the specified delay.
     * Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long delay)
    {
        schedule(delay, 0L);
    }

    /**
     * Schedule the interval to execute repeatedly, with the same delay.
     * Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long delay, boolean repeat)
    {
        schedule(delay, repeat ? delay : 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified
     * initial delay and repeat delay.
     * Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long initialDelay, long repeatDelay)
    {
        if (!_mgr.isRegisteredFrameParticipant(this)) {
            _mgr.registerFrameParticipant(this);
        }

        _repeatDelay = repeatDelay;
        _initDelay = initialDelay;
        _nextTime = -1;
    }

    /**
     * Cancel the current schedule, and ensure that any expirations that
     * are queued up but have not yet run do not run.
     */
    public final void cancel ()
    {
        _mgr.removeFrameParticipant(this);
    }

    /** Time of the next expiration. */
    protected long _nextTime;

    /** Time between expirations. */
    protected long _repeatDelay;

    /** Time between expirations. */
    protected long _initDelay;

    /** The context whose FrameManager we are using. */
    protected FrameManager _mgr;
}
