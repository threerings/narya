//
// $Id: PuzzleProvider.java,v 1.7 2004/10/21 02:54:44 mdb Exp $
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

package com.threerings.puzzle.server;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.game.GameManager;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.data.PuzzleCodes;
import com.threerings.puzzle.data.PuzzleObject;
import com.threerings.puzzle.data.SolitairePuzzleConfig;

/**
 * Handles the server end of the puzzle services.
 */
public class PuzzleProvider
    implements InvocationProvider, PuzzleCodes
{
    /**
     * Constructs a puzzle provider instance.
     */
    public PuzzleProvider (RootDObjectManager omgr, PlaceRegistry plreg)
    {
        _omgr = omgr;
        _plreg = plreg;
    }

    /**
     * Processes a request from a client to start a puzzle.
     */
    public void startPuzzle (
        ClientObject caller, SolitairePuzzleConfig config,
        InvocationService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject user = (BodyObject)caller;

        Log.debug("Processing start puzzle [caller=" + user.who() +
                  ", config=" + config + "].");

        try {
            // just this fellow will be playing
            config.players = new Name[] { user.username };

            // create the game manager and begin its initialization
            // process
            GameManager gmgr = (GameManager)_plreg.createPlace(config, null);

            // the game manager will take care of notifying the player
            // that the game has been created once it has been started up;
            // but we let the caller know that we processed their request
            listener.requestProcessed();

        } catch (InstantiationException ie) {
            Log.warning("Error instantiating puzzle manager " +
                        "[for=" + caller.who() + ", config=" + config + "].");
            Log.logStackTrace(ie);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /** The distributed object manager with which we interoperate. */
    protected RootDObjectManager _omgr;

    /** The place registry with which we interoperate. */
    protected PlaceRegistry _plreg;
}
