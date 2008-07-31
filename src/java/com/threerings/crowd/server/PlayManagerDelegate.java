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

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.PlaceManagerDelegate;

/**
 * The base class for any delegate that wishes to service both {@code GameManager}
 * games and {@code AVRGameManager} games -- or, in fact, any {@code PlaceManager}
 * that implements {@code PlayManager}.
 */
public class PlayManagerDelegate extends PlaceManagerDelegate
{
    @Override
    public void didInit (PlaceConfig config)
    {
        super.didInit(config);
        _gmgr = (PlayManager)_plmgr;
    }

    /**
     * Returns true if the supplied occupant is a player, false if not.
     */
    protected boolean isPlayer (ClientObject occupant)
    {
        return (occupant != null) && _gmgr.isPlayer(occupant);
    }

    /**
     * Returns true if the supplied occupant is an agent, false if not.
     */
    protected boolean isAgent (ClientObject occupant)
    {
        return (occupant != null) && _gmgr.isAgent(occupant);
    }

    /**
     * Checks that the caller in question is a player if the game is not a party game.
     *
     * @return a casted {@link PlayerObject} reference if the method returns at all.
     */
    protected void verifyIsPlayer (ClientObject caller)
        throws InvocationException
    {
        if (!isPlayer(caller)) {
            throw new InvocationException(InvocationCodes.E_ACCESS_DENIED);
        }
    }

    /**
     * Checks that the caller in question is a player if the game is not a party game
     * or an agent for games that use server-side code.
     */
    protected void verifyIsPlayerOrAgent (ClientObject caller)
        throws InvocationException
    {
        if (!isPlayer(caller) && !isAgent(caller)) {
            throw new InvocationException(InvocationCodes.E_ACCESS_DENIED);
        }
    }

    /**
     *  Make sure that the given caller is a player or an agent and can write to the data
     *  of the given playerId.
     *  @return the resolved player object to write to
     **/
    protected BodyObject verifyWritePermission (ClientObject caller, int playerId)
        throws InvocationException
    {
        verifyIsPlayerOrAgent(caller);

        BodyObject player = _gmgr.checkWritePermission(caller, playerId);
        if (player == null) {
            throw new InvocationException(InvocationCodes.ACCESS_DENIED);
        }
        return player;
    }

    /** A reference to our manager. */
    protected PlayManager _gmgr;
}
