//
// $Id: SceneDispatcher.java,v 1.4 2003/06/03 21:41:33 ray Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneMarshaller;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Dispatches requests to the {@link SceneProvider}.
 */
public class SceneDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public SceneDispatcher (SceneProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new SceneMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case SceneMarshaller.MOVE_TO:
            ((SceneProvider)provider).moveTo(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (SceneMoveListener)args[2]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
