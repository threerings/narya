//
// $Id: LobbyProvider.java,v 1.6 2004/08/27 02:12:50 mdb Exp $
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
