//
// $Id: LobbyProvider.java,v 1.5 2002/08/14 19:07:49 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.micasa.lobby.LobbyService.CategoriesListener;
import com.threerings.micasa.lobby.LobbyService.LobbiesListener;

/**
 * Provides access to the server-side implementation of the lobby
 * services.
 */
public interface LobbyProvider extends InvocationProvider
{
    /**
     * Processes a request by the client to obtain a list of the lobby
     * categories available on this server.
     */
    public void getCategories (ClientObject caller,
                               CategoriesListener listener);

    /**
     * Processes a request by the client to obtain a list of lobbies
     * matching the supplied category string.
     */
    public void getLobbies (ClientObject caller, String category,
                            LobbiesListener listener);
}
