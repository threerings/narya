//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.util;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;

/**
 * Adapts the response from a {@code InvocationService.ResultListener} to a {@link ResultListener}.
 * In the event of failure, the failure string is wrapped in an {@link InvocationException}.
 */
public class InvocationAdapter implements InvocationService.ResultListener
{
    public InvocationAdapter (ResultListener<Object> target)
    {
        _target = target;
    }

    // from InvocationService.ResultListener
    public void requestProcessed (Object result)
    {
        _target.requestCompleted(result);
    }

    // from InvocationService.ResultListener
    public void requestFailed (String cause)
    {
        _target.requestFailed(new InvocationException(cause));
    }

    protected ResultListener<Object> _target;
}
