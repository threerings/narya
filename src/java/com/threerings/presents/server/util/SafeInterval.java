//
// $Id: SafeInterval.java,v 1.1 2002/05/24 21:03:15 mdb Exp $

package com.threerings.presents.server.util;

import java.util.Timer;
import java.util.TimerTask;

import com.threerings.presents.server.PresentsDObjectMgr;

/**
 * Used in conjunction with the {@link PresentsDObjectMgr}, this class
 * provides a means by which code can be run on the dobjmgr thread at some
 * point in the future, either as a recurring interval or as a one shot
 * deal. The code is built on top of the standard Java {@link Timer}
 * services.
 *
 * <p> A {@link SafeInterval} instance should be created and then
 * scheduled to run using the standard {@link Timer} services. For
 * example:
 *
 * <pre>
 *     Timer.schedule(new SafeInterval(_omgr) {
 *         public void intervalExpired () {
 *             System.out.println("Foo!");
 *         }
 *      }, 25L * 1000L);
 * </pre>
 */
public abstract class SafeInterval extends TimerTask
{
    /**
     * Creates a safe interval instance that will queue itself up for
     * execution on the supplied dobjmgr when it expires.
     */
    public SafeInterval (PresentsDObjectMgr omgr)
    {
        _omgr = omgr;
    }

    /**
     * Called (on the dobjmgr thread) when the interval period has
     * expired. If this is a recurring interval, this method will be
     * called each time the interval expires (until {@link #cancel} is
     * called on this instance).
     */
    public abstract void intervalExpired ();

    /** Handles the proper scheduling and queueing. */
    public void run ()
    {
        boolean flipped = isFlipped();
        flip();
        if (flipped) {
            intervalExpired();
        } else {
            _omgr.postUnit(this);
        }
    }

    /** Returns whether or not we're running on the timer thread or the
     * dobjmgr thread. It is synchronized because the variable that tracks
     * this state is inspected on both threads. */
    protected synchronized boolean isFlipped ()
    {
        return _flipped;
    }

    /** Flips our mode (see {@link #isFlipped}). It is synchronized
     * because the variable that tracks this state is updated on both
     * threads. */
    public synchronized void flip ()
    {
        _flipped = !_flipped;
    }

    /** The dobjmgr on which we queue ourselves when we expire. */
    protected PresentsDObjectMgr _omgr;

    /** Because {@link #run} is called both when the interval expires and
     * when the dobjmgr invokes us, we have to use this toggle to figure
     * out when to do what. */
    protected boolean _flipped;
}
