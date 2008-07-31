//
// $Id: GameManagerDelegate.java 487 2007-11-10 04:41:44Z mdb $
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

package com.threerings.crowd.server;

import com.threerings.crowd.data.BodyObject;

import com.threerings.presents.data.ClientObject;

/**
 * An interface to be implemented by a {@code PlaceManager} that wishes to host
 * places that have players. This generalizes the idea of a game further than what
 * is afforded by {@code GameManager}, linking it up with {@code AVRGameManager}.
 */
public interface PlayManager
{
    /**
     * Return true if the given client is a player in this place.
     */
    boolean isPlayer (ClientObject client);

    /**
     * Return true if the given client is a server-side agent in this place.
     */
    boolean isAgent (ClientObject client);

    /**
     *  Make sure that the given caller is a player or an agent and can write to the data
     *  of the given playerId.
     *  @return the resolved player object to write to
     **/
    BodyObject checkWritePermission (ClientObject client, int playerId);
}
