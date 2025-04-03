//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;

/**
 * Handles the orderly shutdown of all server services.
 *
 * @deprecated use Lifecycle
 */
@Deprecated @Singleton
public class ShutdownManager
{
    /** Implementers of this interface will be notified when the server is shutting down. */
    public static interface Shutdowner extends Lifecycle.ShutdownComponent
    {
    }

    /** Constraints for use with {@link ShutdownManager#addConstraint}. */
    public static enum Constraint { RUNS_BEFORE, RUNS_AFTER }

    /**
     * Registers an entity that will be notified when the server is shutting down.
     */
    public void registerShutdowner (Shutdowner downer)
    {
        _cycle.addComponent(downer);
    }

    /**
     * Unregisters the shutdowner from hearing when the server is shutdown.
     */
    public void unregisterShutdowner (Shutdowner downer)
    {
        _cycle.removeComponent(downer);
    }

    /**
     * Adds a constraint that a certain shutdowner must be run before another.
     */
    public void addConstraint (Shutdowner lhs, Constraint constraint, Shutdowner rhs)
    {
        switch (constraint) {
        case RUNS_BEFORE:
            _cycle.addShutdownConstraint(lhs, Lifecycle.Constraint.RUNS_BEFORE, rhs);
            break;
        case RUNS_AFTER:
            _cycle.addShutdownConstraint(lhs, Lifecycle.Constraint.RUNS_AFTER, rhs);
            break;
        }
    }

    /**
     * Queues up a request to shutdown on the dobjmgr thread. This method may be safely called from
     * any thread.
     */
    public void queueShutdown ()
    {
        _server.queueShutdown();
    }

    /**
     * Returns true if we're in the process of shutting down.
     */
    public boolean isShuttingDown ()
    {
        return _cycle.isShuttingDown();
    }

    @Inject protected Lifecycle _cycle;
    @Inject protected PresentsServer _server;
}
