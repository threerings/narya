//
// $Id: PuzzleGameProvider.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.puzzle.client.PuzzleGameService;
import com.threerings.puzzle.data.Board;

/**
 * Handles the server side of the puzzle game services.
 */
public interface PuzzleGameProvider extends InvocationProvider
{
    /**
     * Called when the client has sent a {@link
     * PuzzleGameService#updateProgress} service request.
     */
    public void updateProgress (ClientObject caller, int roundId, int[] events);

    /**
     * Called when the client has sent a {@link
     * PuzzleGameService#updateProgressSync} service request.
     */
    public void updateProgressSync (
        ClientObject caller, int roundId, int[] events, Board[] states);
}
