//
// $Id: TimeBaseDispatcher.java,v 1.3 2004/06/22 13:55:25 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TimeBaseService;
import com.threerings.presents.client.TimeBaseService.GotTimeBaseListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.TimeBaseMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link TimeBaseProvider}.
 */
public class TimeBaseDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public TimeBaseDispatcher (TimeBaseProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new TimeBaseMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case TimeBaseMarshaller.GET_TIME_OID:
            ((TimeBaseProvider)provider).getTimeOid(
                source,
                (String)args[0], (GotTimeBaseListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
