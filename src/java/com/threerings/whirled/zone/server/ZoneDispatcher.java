//
// $Id: ZoneDispatcher.java,v 1.1 2002/08/14 19:07:58 mdb Exp $

package com.threerings.whirled.zone.server;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.zone.client.ZoneService;
import com.threerings.whirled.zone.client.ZoneService.ZoneMoveListener;
import com.threerings.whirled.zone.data.ZoneMarshaller;
import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * Dispatches requests to the {@link ZoneProvider}.
 */
public class ZoneDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public ZoneDispatcher (ZoneProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new ZoneMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case ZoneMarshaller.MOVE_TO:
            ((ZoneProvider)provider).moveTo(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue(), (ZoneMoveListener)args[3]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
