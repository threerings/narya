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

package com.threerings.util;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.IntResultListener;
import com.samskivert.util.Invoker;
import com.samskivert.util.Logger;
import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

import static com.threerings.NaryaLog.log;

/**
 * A chainable ResultListener, InvocationListener, and Invoker.Unit all in one.
 *
 * TODO: add some examples of different usages.
 */
public class Resulting<T> extends Invoker.Unit
    implements ResultListener<T>,
        InvocationService.ConfirmListener, InvocationService.ResultListener
{
    /** A handy {@code Object -> Void} function for when you wish to chain to a
      * {@code ResultListener<Void>}. */
    public static final Function<Object, Void> TO_VOID = new Function<Object, Void>() {
        public Void apply (Object o) {
            return null;
        }
    };

    public Resulting (String name)
    {
        super(name);
    }

    public Resulting (String name, Resulting<T> chain)
    {
        this(name, (ResultListener<T>)chain);
    }

    public Resulting (Resulting<T> chain)
    {
        this(chain.toString(), chain);
    }

    public Resulting (String name, ResultListener<T> chain)
    {
        this(name);
        _chain = chain;
    }

    public Resulting (ResultListener<T> chain)
    {
        this(chain.toString(), chain);
    }

    public Resulting (String name, InvocationService.InvocationListener chain)
    {
        this(name);
        _invChain = chain;
    }

    public Resulting (InvocationService.InvocationListener chain)
    {
        this(chain.toString(), chain);
    }

    public <Q> Resulting (
        String name, final ResultListener<Q> chain, final Function<? super T, ? extends Q> xform)
    {
        this(name, new ResultListener<T>() {
            public void requestCompleted (T result) {
                chain.requestCompleted(xform.apply(result));
            }
            public void requestFailed (Exception cause) {
                chain.requestFailed(cause);
            }
        });
    }

    public <Q> Resulting (ResultListener<Q> chain, Function<? super T, ? extends Q> xform)
    {
        this(chain.toString(), chain, xform);
    }

    public Resulting (String name, Logger log, Object... logArgs)
    {
        this(name);
        _log = log;
        _logArgs = logArgs;
    }

    /**
     * Construct your {@code Resulting<Integer>}, overriding what you need to, then call this to
     * adapt it.
     */
    public final IntResultListener asIntResultListener ()
    {
        return new IntResultListener() {
            public void requestCompleted (int result) {
                // call the invocation method and it will do the unsafe cast for us.
                Resulting.this.requestProcessed((Object)result);
            }
            public void requestFailed (Exception cause) {
                Resulting.this.requestFailed(cause);
            }
        };
    }

// TODO: Maybe we don't extend Invoker.Unit
//    public Invoker.Unit asUnit (Callable<T> callable)
//    {
//    }
//
//    public Invoker.Unit asUnit ()
//    {
//    }
//
    /**
     * Override this if you are using a Resulting as an Invoker unit.
     */
    public T invokePersist ()
        throws Exception
    {
        // please to be overriding
        return null;
    }

    @Override
    public final boolean invoke ()
    {
        try {
            _result = invokePersist();

        } catch (StopException se) {
            return false;

        } catch (Exception e) {
            _error = e;
        }
        return true;
    }

    /**
     * Call this from your invokePersist() to suppress returning to the dobj thread.
     * @throws StopException every time.
     */
    protected final T stop ()
        throws StopException
    {
        throw STOP.get();
    }

    @Override
    public final void handleResult ()
    {
        if (_error != null) {
            requestFailed(_error);
        } else {
            requestCompleted(_result);
        }
    }

    // from InvocationService.InvocationListener
    public final void requestFailed (String error)
    {
        requestFailed(new InvocationException(error));
    }

    // from InvocationService.ConfirmListener
    public final void requestProcessed ()
    {
        requestCompleted(null);
    }

    // from InvocationService.ResultListener
    public final void requestProcessed (Object result)
    {
        @SuppressWarnings("unchecked") T casted = (T)result;
        requestCompleted(casted);
    }

    /**
     * Override this to handle a request failed in your own way.
     */
    public void requestFailed (Exception cause)
    {
        if (_chain != null) {
            _chain.requestFailed(cause);

        } else if (_invChain != null) {
            _invChain.requestFailed((cause instanceof InvocationException)
                ? cause.getMessage()
                : InvocationCodes.INTERNAL_ERROR);

        } else {
            Object[] logArgs = MoreObjects.firstNonNull(_logArgs, ArrayUtil.EMPTY_OBJECT);
            Object[] args;
            if (cause instanceof InvocationException) {
                args = new Object[logArgs.length + 4];
                args[args.length - 2] = "error";
                args[args.length - 1] = cause.getMessage();

            } else {
                args = new Object[logArgs.length + 3];
                args[args.length - 1] = cause;
            }
            args[0] = "Resulting";
            args[1] = this;
            System.arraycopy(logArgs, 0, args, 2, logArgs.length);
            MoreObjects.firstNonNull(_log, log).warning("Request failed", args);
        }
    }

    /**
     * Override this to handle a request completion in your own way.
     */
    public void requestCompleted (T result)
    {
        if (_chain != null) {
            _chain.requestCompleted(result);

        } else if (_invChain instanceof InvocationService.ResultListener) {
            ((InvocationService.ResultListener)_invChain).requestProcessed(result);

        } else if (_invChain instanceof InvocationService.ConfirmListener) {
            ((InvocationService.ConfirmListener)_invChain).requestProcessed();
        }
    }

    /** Our invocation chain, if any. */
    protected InvocationService.InvocationListener _invChain;

    protected ResultListener<T> _chain;

    protected Logger _log;

    protected Object[] _logArgs;

    protected T _result;

    protected Exception _error;

    protected static final ThreadLocal<StopException> STOP =
        new ThreadLocal<StopException>() {
            @Override protected StopException initialValue () {
                return new StopException();
            }
        };

    protected static class StopException extends RuntimeException
    {
    }
}
