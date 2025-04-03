//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.util.concurrent.Executor;

import com.samskivert.util.Interval;
import com.samskivert.util.RunQueue;

/**
 * The root distributed object manager extends the basic distributed object manager interface with
 * methods that can only be guaranteed to work in the virtual machine that is hosting the
 * distributed objects in question. VMs that operate proxies of objects can only implement the
 * basic distributed object manager interface.
 */
public interface RootDObjectManager extends DObjectManager, RunQueue, Executor, Interval.Factory
{
    /**
     * Looks up and returns the requested distributed object in the dobj table, returning null if
     * no object exists with that oid.
     */
    DObject getObject (int oid);

    /**
     * Registers a distributed object instance of the supplied class with the system and assigns it
     * an oid. When the call returns the object will be registered with the system and its oid will
     * have been assigned.
     *
     * @return the registered object for the caller's convenience.
     */
    <T extends DObject> T registerObject (T object);

    /**
     * Requests that the specified object be destroyed. Once destroyed an object is removed from
     * the runtime system and may no longer have events dispatched on it.
     *
     * @param oid The object id of the distributed object to be destroyed.
     */
    void destroyObject (int oid);

    /**
     * Creates an {@link Interval} that runs the supplied runnable. If the root omgr is shutdown
     * before the interval expires (or if the interval is scheduled to repeat), it will be
     * automatically cancelled. This makes it easy to schedule fire-and-forget intervals:
     *
     * <pre>
     * _omgr.newInterval(someRunnable).schedule(500); // one shot
     * Interval ival = _omgr.newInterval(someRunnable).schedule(500, true); // repeater
     * </pre>
     */
    Interval newInterval (Runnable action);
}
