//
// $Id: LobbyDispatcher.java,v 1.2 2002/08/20 19:38:14 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.micasa.lobby.LobbyMarshaller;
import com.threerings.micasa.lobby.LobbyService;
import com.threerings.micasa.lobby.LobbyService.CategoriesListener;
import com.threerings.micasa.lobby.LobbyService.LobbiesListener;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import java.util.List;

/**
 * Dispatches requests to the {@link LobbyProvider}.
 *
 * <p> Generated from <code>
 * $Id: LobbyDispatcher.java,v 1.2 2002/08/20 19:38:14 mdb Exp $
 * </code>
 */
public class LobbyDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public LobbyDispatcher (LobbyProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new LobbyMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case LobbyMarshaller.GET_CATEGORIES:
            ((LobbyProvider)provider).getCategories(
                source,
                (CategoriesListener)args[0]
            );
            return;

        case LobbyMarshaller.GET_LOBBIES:
            ((LobbyProvider)provider).getLobbies(
                source,
                (String)args[0], (LobbiesListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
