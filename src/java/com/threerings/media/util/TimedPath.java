//
// $Id: TimedPath.java,v 1.3 2003/01/17 22:57:08 mdb Exp $

package com.threerings.media.util;

/**
 * A base class for path implementations that endeavor to move their
 * pathables along a path in a specified number of milliseconds.
 */
public abstract class TimedPath implements Path
{
    /**
     * Configures the timed path with the duration in which it must
     * traverse its path.
     */
    public TimedPath (long duration)
    {
        // sanity check some things
        if (duration <= 0) {
            String errmsg = "Requested path with illegal duration (<=0) " +
                "[duration=" + duration + "]";
            throw new IllegalArgumentException(errmsg);
        }

        _duration = duration;
    }

    // documentation inherited
    public void init (Pathable pable, long timestamp)
    {
        // give the pable a chance to perform any starting antics
        pable.pathBeginning();

        // make a note of when we started
        _startStamp = timestamp;

        // we'll be ticked immediately following init() which will update
        // our position to the start of our path
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _startStamp += timeDelta;
    }

    // documentation inherited from interface
    public void wasRemoved (Pathable pable)
    {
        // nothing doing
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An extensible method for generating a string representation of this
     * instance.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("duration=").append(_duration).append("ms");
    }

    /** The duration that we're to spend following the path. */
    protected long _duration;

    /** The time at which we started along the path. */
    protected long _startStamp;
}
