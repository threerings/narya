//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import static com.threerings.presents.Log.log;

/**
 * Provides the basic functionality used to dispatch invocation notification events.
 */
public abstract class InvocationDecoder
{
    /** The receiver for which we're decoding and dispatching notifications. */
    public InvocationReceiver receiver;

    /**
     * Returns the generated hash code that is used to identify this invocation notification
     * service.
     */
    public abstract String getReceiverCode ();

    /**
     * Dispatches the specified method to our receiver.
     */
    public void dispatchNotification (int methodId, Object[] args)
    {
        log.warning("Requested to dispatch unknown method", "receiver", receiver,
                    "methodId", methodId, "args", args);
    }

    /**
     * Performs type casts in a way that works for parameterized types as well as simple types.
     */
    @SuppressWarnings("unchecked")
    protected <T> T cast (Object value)
    {
        return (T)value;
    }
}
