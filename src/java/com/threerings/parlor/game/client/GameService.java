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

package com.threerings.parlor.game.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides services used by game clients to request that actions be taken
 * by the game manager.
 */
public interface GameService extends InvocationService
{
    /**
     * Lets the game manager know that the calling player is in the game
     * room and ready to play.
     */
    public void playerReady (Client client);

    /**
     * Asks the game manager to start the party game.  This should only be
     * called for party games, and then only by the creating player after
     * any other game-specific starting prerequisites (e.g., a required
     * number of players) have been fulfilled.
     */
    public void startPartyGame (Client client);
}
