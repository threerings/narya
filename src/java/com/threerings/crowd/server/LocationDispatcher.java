//
// $Id: LocationDispatcher.java,v 1.2 2002/08/20 19:38:14 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.crowd.client.LocationService;
import com.threerings.crowd.client.LocationService.MoveListener;
import com.threerings.crowd.data.LocationMarshaller;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link LocationProvider}.
 *
 * <p> Generated from <code>
 * $Id: LocationDispatcher.java,v 1.2 2002/08/20 19:38:14 mdb Exp $
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
