//
// $Id: ConfirmAdapter.java,v 1.1 2003/06/26 18:24:31 mdb Exp $

package com.threerings.presents.util;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

/**
 * Adapts the response from a {@link ResultListener} to a {@link
 * ConfirmListener} if the failure is an instance fo {@link
 * InvocationException} the message will be passed on to the confirm
 * listener, otherwise they will be provided with {@link
 * InvocationCodes#INTERNAL_ERROR}.
 */
public class ConfirmAdapter implements ResultListener
{
    /**
     * Creates an adapter with the supplied listener.
     */
    public ConfirmAdapter (ConfirmListener listener)
    {
        _listener = listener;
    }

    // documentation inherited from interface
    public void requestCompleted (Object result)
    {
        _listener.requestProcessed();
    }

    // documentation inherited from interface
    public void requestFailed (Exception cause)
    {
        if (cause instanceof InvocationException) {
            _listener.requestFailed(cause.getMessage());
        } else {
            _listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
        }
    }

    protected ConfirmListener _listener;
}
