//
// $Id: LobbyProvider.java,v 1.1 2001/10/04 00:29:07 mdb Exp $

package com.threerings.micasa.lobdy;

import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.party.data.BodyObject;

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
     * Processes a request by the client to obtain a list of lobbies
     * matching the supplied pattern string.
     */
    public void handleGetLobbiesRequest (
        BodyObject source, int invid, String pattern)
    {
    }

    /** A reference to the lobby registry. */
    protected LobbyRegistry _lobreg;
}
