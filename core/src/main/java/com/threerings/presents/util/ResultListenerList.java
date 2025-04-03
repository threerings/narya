//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.util;

import java.util.ArrayList;

import com.threerings.presents.client.InvocationService;

/**
 * Maintains a list of result listeners, dispatching the eventual actual result or failure to them
 * all as if they were a single listener.
 */
public class ResultListenerList extends ArrayList<InvocationService.ResultListener>
    implements InvocationService.ResultListener
{
    // from InvocationService.ResultListener
    public void requestProcessed (Object result)
    {
        for (InvocationService.ResultListener listener : this) {
            listener.requestProcessed(result);
        }
    }

    // from InvocationService.ResultListener
    public void requestFailed (String cause)
    {
        for (InvocationService.ResultListener listener : this) {
            listener.requestFailed(cause);
        }
    }
}
