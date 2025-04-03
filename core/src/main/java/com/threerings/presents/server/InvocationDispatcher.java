//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import static com.threerings.presents.Log.log;

/**
 * Provides the base class via which invocation service requests are dispatched.
 */
public abstract class InvocationDispatcher<T extends InvocationMarshaller<?>>
    implements InvocationManager.Dispatcher
{
    /** The invocation provider for whom we're dispatching. */
    public InvocationProvider provider;

    /**
     * Creates an instance of the appropriate {@link InvocationMarshaller} derived class for use
     * with this dispatcher.
     */
    public abstract T createMarshaller ();

    // from interface InvocationManager.Dispatcher
    public InvocationProvider getProvider () {
        return provider;
    }

    /**
     * Dispatches the specified method to our provider.
     */
    public void dispatchRequest (ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        log.warning("Requested to dispatch unknown method", "provider", provider,
                    "sourceOid", source.getOid(), "methodId", methodId, "args", args);
    }

    /**
     * Performs type casts in a way that works for parameterized types as well as simple types.
     */
    @SuppressWarnings("unchecked")
    protected <K> K cast (Object value)
    {
        return (K)value;
    }
}
