//
// $Id: LobbyProvider.java,v 1.3 2001/10/11 04:13:33 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.util.StreamableArrayList;
import com.threerings.crowd.data.BodyObject;

/**
 * Handles server side of lobby-related invocation services.
 */
public class LobbyProvider
    extends InvocationProvider implements LobbyCodes
{
    /**
     * Constructs a lobby provider instance which will be used to handle
     * all lobby-related invocation service requests. This is
     * automatically taken care of by the lobby registry, so no other
     * entity need instantiate and register a lobby provider.
     *
     * @param lobreq a reference to the lobby registry active in this
     * server.
     */
    public LobbyProvider (LobbyRegistry lobreg)
    {
        _lobreg = lobreg;
    }

    /**
     * Processes a request by the client to obtain a list of the lobby
     * categories available on this server.
     */
    public void handleGetCategoriesRequest (
        BodyObject source, int invid)
    {
        String[] cats = _lobreg.getCategories(source);
        // we have to cast the array to an object to avoid having the
        // compiler pick the version of sendResponse that takes Object[]
        sendResponse(source, invid, GOT_CATEGORIES_RESPONSE, (Object)cats);
    }

    /**
     * Processes a request by the client to obtain a list of lobbies
     * matching the supplied category string.
     */
    public void handleGetLobbiesRequest (
        BodyObject source, int invid, String category)
    {
        StreamableArrayList list = new StreamableArrayList();
        _lobreg.getLobbies(source, category, list);
        sendResponse(source, invid, GOT_LOBBIES_RESPONSE, list);
    }

    /** A reference to the lobby registry. */
    protected LobbyRegistry _lobreg;
}
