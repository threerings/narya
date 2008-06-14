//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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


import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.ObserverList;
import com.samskivert.util.RunQueue;

import com.threerings.presents.annotation.EventQueue;
import com.threerings.util.DependencyGraph;

import static com.threerings.presents.Log.log;

/**
 * Handles the orderly shutdown of all server services.
 */
@Singleton
public class ShutdownManager
{
    /** Implementers of this interface will be notified when the server is shutting down. */
    public static interface Shutdowner
    {
        /**
         * Called when the server is shutting down.
         */
        public void shutdown ();
    }

    public static enum Constraint { RUNS_BEFORE, RUNS_AFTER };

    @Inject ShutdownManager (@EventQueue RunQueue dobjq)
    {
        _dobjq = dobjq;
    }

    /**
     * Registers an entity that will be notified when the server is shutting down.
     */
    public void registerShutdowner (Shutdowner downer)
    {
        _downers.add(downer);
    }

    /**
     * Unregisters the shutdowner from hearing when the server is shutdown.
     */
    public void unregisterShutdowner (Shutdowner downer)
    {
        _downers.remove(downer);
    }

    /**
     * Adds a constraint that a certain shutdowner must be run before another.
     */
    public void addConstraint (Shutdowner lhs, Constraint constraint, Shutdowner rhs)
        throws IllegalArgumentException
    {
        if (lhs == null || rhs == null) {
            throw new IllegalArgumentException("Cannot add constraint about null shutdowner.");
        }
        Shutdowner before = (constraint == Constraint.RUNS_BEFORE) ? lhs : rhs;
        Shutdowner after = (constraint == Constraint.RUNS_BEFORE) ? rhs : lhs;
        _downers.addDependency(after, before);
    }

    /**
     * Queues up a request to shutdown on the dobjmgr thread. This method may be safely called from
     * any thread.
     */
    public void queueShutdown ()
    {
        _dobjq.postRunnable(new Runnable() {
            public void run () {
                shutdown();
            }
        });
    }

    /**
     * Shuts down all shutdowners immediately on the caller's thread.
     */
    public void shutdown ()
    {
        if (_downers == null) {
            log.warning("Refusing repeat shutdown request.");
            return;
        }

        ObserverList<Shutdowner> downers = ObserverList.newSafeInOrder();

        while (!_downers.isEmpty()) {
            downers.add(_downers.removeAvailableElement());
        }

        _downers = null;

        // shut down all shutdown participants
        downers.apply(new ObserverList.ObserverOp<Shutdowner>() {
            public boolean apply (Shutdowner downer) {
                downer.shutdown();
                return true;
            }
        });
    }

    /** All of the registered shutdowners along with related constraints. */
    protected DependencyGraph<Shutdowner> _downers = new DependencyGraph<Shutdowner>();

    /** The queue we'll use to get onto the dobjmgr thread before shutting down. */
    protected RunQueue _dobjq;

}
