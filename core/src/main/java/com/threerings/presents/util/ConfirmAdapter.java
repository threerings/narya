//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.util;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

/**
 * Adapts the response from a {@link ResultListener} to a {@link ConfirmListener} if the failure is
 * an instance of {@link InvocationException} the message will be passed on to the confirm
 * listener, otherwise they will be provided with {@link InvocationCodes#INTERNAL_ERROR}.
 */
public class ConfirmAdapter extends IgnoreConfirmAdapter<Void>
{
    /**
     * Creates an adapter with the supplied listener.
     */
    public ConfirmAdapter (InvocationService.ConfirmListener listener)
    {
        super(listener);
    }
}
