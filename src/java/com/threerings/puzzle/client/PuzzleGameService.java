//
// $Id: PuzzleGameService.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

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
     * {@link #SYNC_BOARD_STATE} is true and which includes the board
     * states associated with each event.
     */
    public void updateProgressSync (
        Client client, int roundId, int[] events, Board[] states);
}
