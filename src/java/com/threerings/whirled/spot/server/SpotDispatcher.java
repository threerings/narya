//
// $Id: SpotDispatcher.java,v 1.2 2002/08/20 19:38:15 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneMarshaller.SceneMoveMarshaller;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.client.SpotService.ChangeLocListener;
import com.threerings.whirled.spot.data.SpotMarshaller;

/**
 * Dispatches requests to the {@link SpotProvider}.
 *
 * <p> Generated from <code>
 * $Id: SpotDispatcher.java,v 1.2 2002/08/20 19:38:15 mdb Exp $
 * </code>
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
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue(), (SceneMoveListener)args[3]
            );
            return;

        case SpotMarshaller.CHANGE_LOC:
            ((SpotProvider)provider).changeLoc(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (ChangeLocListener)args[2]
            );
            return;

        case SpotMarshaller.CLUSTER_SPEAK:
            ((SpotProvider)provider).clusterSpeak(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (String)args[2], ((Byte)args[3]).byteValue()
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
