//
// $Id: SpotDispatcher.java,v 1.4 2003/03/26 02:06:06 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneMarshaller.SceneMoveMarshaller;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.SpotMarshaller;

/**
 * Dispatches requests to the {@link SpotProvider}.
 */
public class SpotDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public SpotDispatcher (SpotProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new SpotMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case SpotMarshaller.TRAVERSE_PORTAL:
            ((SpotProvider)provider).traversePortal(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (SceneMoveListener)args[2]
            );
            return;

        case SpotMarshaller.CHANGE_LOCATION:
            ((SpotProvider)provider).changeLocation(
                source,
                (Location)args[0], (ConfirmListener)args[1]
            );
            return;

        case SpotMarshaller.JOIN_CLUSTER:
            ((SpotProvider)provider).joinCluster(
                source,
                ((Integer)args[0]).intValue(), (ConfirmListener)args[1]
            );
            return;

        case SpotMarshaller.CLUSTER_SPEAK:
            ((SpotProvider)provider).clusterSpeak(
                source,
                (String)args[0], ((Byte)args[1]).byteValue()
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
