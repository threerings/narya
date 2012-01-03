//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.presents.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;

/**
 * Provides the result of an invocation service request as a future result. Modeled on and mostly
 * implemented by code from {@link FutureTask}.
 */
public class FutureResult<V>
    implements Future<V>, InvocationService.ResultListener
{
    // from interface Future
    public boolean cancel (boolean mayInterruptIfRunning)
    {
        return _sync.innerCancel(mayInterruptIfRunning);
    }

    // from interface Future
    public boolean isCancelled ()
    {
        return _sync.innerIsCancelled();
    }

    // from interface Future
    public boolean isDone ()
    {
        return _sync.innerIsDone();
    }

    // from interface Future
    public V get () throws InterruptedException, ExecutionException
    {
        return _sync.innerGet();
    }

    // from interface Future
    public V get (long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return _sync.innerGet(unit.toNanos(timeout));
    }

    // from interface InvocationService.ResultListener
    public void requestProcessed (Object result)
    {
        @SuppressWarnings("unchecked") V value = (V)result;
        _sync.innerSet(value);
    }

    // from interface InvocationService.ResultListener
    public void requestFailed (String cause)
    {
        _sync.innerSetException(new InvocationException(cause));
    }

    protected class Sync extends AbstractQueuedSynchronizer
    {
        public boolean innerIsCancelled () {
            return getState() == CANCELLED;
        }

        public boolean innerIsDone () {
            return ranOrCancelled(getState());
        }

        public V innerGet() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);
            if (getState() == CANCELLED) {
                throw new CancellationException();
            }
            if (_exception != null) {
                throw new ExecutionException(_exception);
            }
            return _result;
        }

        public V innerGet (long nanosTimeout)
            throws InterruptedException, ExecutionException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout)) {
                throw new TimeoutException();
            }
            if (getState() == CANCELLED) {
                throw new CancellationException();
            }
            if (_exception != null) {
                throw new ExecutionException(_exception);
            }
            return _result;
        }

        public void innerSet (V v) {
            for (;;) {
                int s = getState();
                if (s == RAN) {
                    return;
                }
                if (s == CANCELLED) {
                    releaseShared(0);
                    return;
                }
                if (compareAndSetState(s, RAN)) {
                    _result = v;
                    releaseShared(0);
                    return;
                }
            }
        }

        public void innerSetException (Throwable t) {
            for (;;) {
                int s = getState();
                if (s == RAN) {
                    return;
                }
                if (s == CANCELLED) {
                    releaseShared(0);
                    return;
                }
                if (compareAndSetState(s, RAN)) {
                    _exception = t;
                    _result = null;
                    releaseShared(0);
                    return;
                }
            }
        }

        public boolean innerCancel (boolean mayInterruptIfRunning) {
            for (;;) {
                int s = getState();
                if (ranOrCancelled(s)) {
                    return false;
                }
                if (compareAndSetState(s, CANCELLED)) {
                    break;
                }
            }
            releaseShared(0);
            return true;
        }

        @Override protected int tryAcquireShared (int ignore) {
            return innerIsDone()? 1 : -1;
        }

        @Override protected boolean tryReleaseShared (int ignore) {
            return true;
        }

        protected boolean ranOrCancelled (int state) {
            return (state & (RAN | CANCELLED)) != 0;
        }

        /** The result to return from get() */
        protected V _result;

        /** The exception to throw from get() */
        protected Throwable _exception;

        protected static final int RUNNING = 1;
        protected static final int RAN = 2;
        protected static final int CANCELLED = 4;
    }

    protected Sync _sync = new Sync();
}
