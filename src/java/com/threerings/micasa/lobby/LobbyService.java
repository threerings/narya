//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
