//
// $Id: LobbyService.java,v 1.3 2002/05/15 23:54:34 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.micasa.Log;

/**
 * This class provides an interface to the various parlor services that
 * are directly invokable by the client (by means of the invocation
 * services).
 */
public class LobbyService
    implements LobbyCodes
{
    /**
     * Requests the list of lobby cateogories that are available on this
     * server.
     *
     * @param client a connected, operational client instance.
     * @param rsptarget the object reference that will receive and process
     * the response. The response will come in the form of a method call
     * to <code>handleGotCategories</code> or
     * <code>handleRequestFailed</code>.
     *
     * @return the invocation request id of the generated request.
     */
    public static int getCategories (Client client, Object rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Log.debug("Sending get categories.");
        return invdir.invoke(
            MODULE_NAME, GET_CATEGORIES_REQUEST, null, rsptarget);
    }

    /**
     * Requests information on all active lobbies that match the specified
     * category.
     *
     * @param client a connected, operational client instance.
     * @param category the category of game for which a list of lobbies is
     * desired.
     * @param rsptarget the object reference that will receive and process
     * the response. The response will come in the form of a method call
     * to <code>handleLobbyList</code> or
     * <code>handleRequestFailed</code>.
     *
     * @return the invocation request id of the generated request.
     */
    public static int getLobbies (
        Client client, String category, Object rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { category };
        Log.debug("Sending get lobbies [category=" + category + "].");
        return invdir.invoke(
            MODULE_NAME, GET_LOBBIES_REQUEST, args, rsptarget);
    }
}
