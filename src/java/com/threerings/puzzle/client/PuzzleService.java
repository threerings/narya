//
// $Id: PuzzleService.java,v 1.3 2004/07/10 04:17:21 mdb Exp $

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
