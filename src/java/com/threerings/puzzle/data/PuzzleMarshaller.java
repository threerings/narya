//
// $Id: PuzzleMarshaller.java,v 1.5 2004/10/21 02:54:44 mdb Exp $

package com.threerings.puzzle.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.puzzle.client.PuzzleService;
import com.threerings.puzzle.data.SolitairePuzzleConfig;

/**
 * Provides the implementation of the {@link PuzzleService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class PuzzleMarshaller extends InvocationMarshaller
    implements PuzzleService
{
    /** The method id used to dispatch {@link #startPuzzle} requests. */
    public static final int START_PUZZLE = 1;

    // documentation inherited from interface
    public void startPuzzle (Client arg1, SolitairePuzzleConfig arg2, ConfirmListener arg3)
    {
        ConfirmMarshaller listener3 = new ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, START_PUZZLE, new Object[] {
            arg2, listener3
        });
    }

}
