//
// $Id: SceneDispatcher.java,v 1.2 2002/08/20 19:38:15 mdb Exp $

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

/**
 * Dispatches requests to the {@link SceneProvider}.
 *
 * <p> Generated from <code>
 * $Id: SceneDispatcher.java,v 1.2 2002/08/20 19:38:15 mdb Exp $
 * </code>
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
