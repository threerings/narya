//
// $Id: LobbyService.java,v 1.4 2002/08/14 19:07:49 mdb Exp $

package com.threerings.micasa.lobby;

import java.util.List;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides an interface to the various parlor services that are directly
 * invokable by the client (by means of the invocation services).
 */
public interface LobbyService extends InvocationService
{
    /**
     * Used to communicate the results of a {@link #getCategories}
     * request.
     */
    public static interface CategoriesListener extends InvocationListener
    {
        /**
         * Supplies the listener with the results of a {@link
         * #getCategories} request.
         */
        public void gotCategories (String[] categories);
    }

    /**
     * Used to communicate the results of a {@link #getLobbies}
     * request.
     */
    public static interface LobbiesListener extends InvocationListener
    {
        /**
         * Supplies the listener with the results of a {@link
         * #getLobbies} request.
         */
        public void gotLobbies (List lobbies);
    }

    /**
     * Requests the list of lobby cateogories that are available on this
     * server.
     *
     * @param client a connected, operational client instance.
     * @param listener the listener that will receive and process the
     * response.
     */
    public void getCategories (Client client, CategoriesListener listener);

    /**
     * Requests information on all active lobbies that match the specified
     * category.
     *
     * @param client a connected, operational client instance.
     * @param category the category of game for which a list of lobbies is
     * desired.
     * @param listener the listener that will receive and process the
     * response.
     */
    public void getLobbies (Client client, String category,
                            LobbiesListener listener);
}
