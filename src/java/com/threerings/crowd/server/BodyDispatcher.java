//
// $Id: BodyDispatcher.java,v 1.4 2004/06/22 13:57:44 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.crowd.client.BodyService;
import com.threerings.crowd.data.BodyMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link BodyProvider}.
 */
public class BodyDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public BodyDispatcher (BodyProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new BodyMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case BodyMarshaller.SET_IDLE:
            ((BodyProvider)provider).setIdle(
                source,
                ((Boolean)args[0]).booleanValue()
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
