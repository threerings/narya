//
// $Id: PuzzleGameService.java,v 1.3 2004/10/28 19:20:04 mdb Exp $
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

import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.PuzzleCodes;

/**
 * Provides services used by puzzle game clients to request that actions
 * be taken by the puzzle manager.
 */
public interface PuzzleGameService extends InvocationService, PuzzleCodes
{
    /**
     * Asks the puzzle manager to apply the supplied progress events for
     * the specified puzzle round to the player's state.
     */
    public void updateProgress (Client client, int roundId, int[] events);

    /**
     * Debug variant of {@link #updateProgress} that is only used when
     * {@link PuzzlePanel#isSyncingBoards} is true and which includes the
     * board states associated with each event.
     */
    public void updateProgressSync (
        Client client, int roundId, int[] events, Board[] states);
}
