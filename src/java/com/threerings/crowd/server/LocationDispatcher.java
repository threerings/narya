//
// $Id: LocationDispatcher.java,v 1.3 2004/02/25 14:41:47 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.crowd.client.LocationService.MoveListener;
import com.threerings.crowd.data.LocationMarshaller;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link LocationProvider}.
 *
 * <p> Generated from <code>
 * $Id: LocationDispatcher.java,v 1.3 2004/02/25 14:41:47 mdb Exp $
 * </code>
 */
public class LocationDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public LocationDispatcher (LocationProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new LocationMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case LocationMarshaller.MOVE_TO:
            ((LocationProvider)provider).moveTo(
                source,
                ((Integer)args[0]).intValue(), (MoveListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
