//
// $Id: SafeInterval.java,v 1.2 2003/11/12 23:37:47 mdb Exp $

package com.threerings.presents.client.util;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

import com.threerings.presents.client.Client;

/**
 * Used in conjunction with the {@link Client}, this class provides a
 * means by which code can be run on the client main thread at some point
 * in the future, either as a recurring interval or as a one shot deal.
 * The code is built on top of the {@link IntervalManager} services.
 *
 * <p> A {@link SafeInterval} instance should be created and then
 * scheduled to run using the {@link IntervalManager}. For example:
 *
 * <pre>
 *     IntervalManager.register(new SafeInterval(_ctx.getClient()) {
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
     * execution using the supplied client when it expires.
     */
    public SafeInterval (Client client)
    {
        _client = client;
    }

    /**
     * Called (on the client main thread) when the interval period has
     * expired. If this is a recurring interval, this method will be
     * called each time the interval expires.
     */
    public abstract void run ();

    /** Handles the proper scheduling and queueing. */
    public void intervalExpired (int id, Object arg)
    {
        _iid = id;
        _client.getInvoker().invokeLater(this);
    }

    /** The client via which we queue ourselves when we expire. */
    protected Client _client;

    /** Configured with our current interval id when we are triggered. */
    protected int _iid;
}
