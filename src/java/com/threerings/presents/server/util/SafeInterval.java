//
// $Id: SafeInterval.java,v 1.4 2004/08/27 02:20:25 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.server.util;

import com.samskivert.util.Interval;

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
