//
// $Id: PuzzleService.java,v 1.4 2004/08/27 02:20:27 mdb Exp $
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

package com.threerings.puzzle.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.puzzle.data.SolitairePuzzleConfig;

/**
 * The puzzle services provide a mechanism by which the client can enter
 * and leave puzzles. These services should not be used directly, but
 * instead should be accessed via the {@link PuzzleDirector}.
 */
public interface PuzzleService extends InvocationService
{
    /** Used to communicate responses to {@link #enterPuzzle} requests. */
    public static interface EnterPuzzleListener extends InvocationListener
    {
        /**
         * Indicates that a {@link #enterPuzzle} request was successful
         * and provides the place config for the puzzle room.
         */
        public void puzzleEntered (PlaceConfig config);
    }

    /**
     * Requests that this client start up the specified single-player
     * puzzle.
     */
    public void startPuzzle (Client client, SolitairePuzzleConfig config,
                             ConfirmListener listener);

    /**
     * Requests that this client enter the specified puzzle.
     */
    public void enterPuzzle (
        Client client, int puzzleOid, EnterPuzzleListener listener);

    /**
     * Requests that this client depart whatever puzzle they occupy.
     */
    public void leavePuzzle (Client client);
}
