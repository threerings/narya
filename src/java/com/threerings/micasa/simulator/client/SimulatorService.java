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

package com.threerings.micasa.simulator.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides access to simulator invocation services.
 */
public interface SimulatorService extends InvocationService
{
    /**
     * Requests that a new game be created.
     *
     * @param client a connected, operational client instance.
     * @param config the game config for the game to be created.
     * @param simClass the class name of the simulant to create.
     * @param playerCount the number of players in the game.
     */
    public void createGame (Client client, GameConfig config,
                            String simClass, int playerCount);
}
