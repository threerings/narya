//
// $Id: SafeInterval.java,v 1.2 2002/05/24 21:38:24 mdb Exp $

package com.threerings.presents.server.util;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

import com.threerings.presents.server.PresentsDObjectMgr;

/**
 * Used in conjunction with the {@link PresentsDObjectMgr}, this class
 * provides a means by which code can be run on the dobjmgr thread at some
 * point in the future, either as a recurring interval or as a one shot
 * deal. The code is built on top of the {@link IntervalManager} services.
 *
 * <p> A {@link SafeInterval} instance should be created and then
 * scheduled to run using the {@link IntervalManager}. For example:
 *
 * <pre>
 *     IntervalManager.register(new SafeInterval(_omgr) {
 *         public void run () {
 *             System.out.println("Foo!");
 *         }
 *      }, 25L * 1000L, null, false);
 * </pre>
 */
public abstract class SafeInterval
    implements Runnable, Interval
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
     * called each time the interval expires.
     */
    public abstract void run ();

    /** Handles the proper scheduling and queueing. */
    public void intervalExpired (int id, Object arg)
    {
        _omgr.postUnit(this);
    }

    /** The dobjmgr on which we queue ourselves when we expire. */
    protected PresentsDObjectMgr _omgr;
}
