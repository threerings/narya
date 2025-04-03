//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.util;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

import static com.threerings.presents.Log.log;

/**
 * Adapts the response from a {@link ResultListener} to an InvocationService.ResultListener if the
 * failure is an instance of {@link InvocationException} the message will be passed on to the
 * result listener, otherwise they will be provided with {@link InvocationCodes#INTERNAL_ERROR}.
 *
 * @param <T> the type of result expected by the listener.
 */
public class ResultAdapter<T> implements ResultListener<T>
{
    /**
     * Creates an adapter with the supplied listener.
     */
    public ResultAdapter (InvocationService.ResultListener listener)
    {
        _listener = listener;
    }

    // documentation inherited from interface
    public void requestCompleted (T result)
    {
        _listener.requestProcessed(result);
    }

    // documentation inherited from interface
    public void requestFailed (Exception cause)
    {
        if (cause instanceof InvocationException) {
            _listener.requestFailed(cause.getMessage());
        } else {
            log.warning(cause);
            _listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
        }
    }

    protected InvocationService.ResultListener _listener;
}
