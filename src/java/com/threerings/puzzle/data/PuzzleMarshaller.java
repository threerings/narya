//
// $Id: PuzzleMarshaller.java,v 1.3 2004/07/10 04:17:21 mdb Exp $

package com.threerings.puzzle.data;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.puzzle.client.PuzzleService;
import com.threerings.puzzle.client.PuzzleService.EnterPuzzleListener;
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
    // documentation inherited
    public static class EnterPuzzleMarshaller extends ListenerMarshaller
        implements EnterPuzzleListener
    {
        /** The method id used to dispatch {@link #puzzleEntered}
         * responses. */
        public static final int PUZZLE_ENTERED = 1;

        // documentation inherited from interface
        public void puzzleEntered (PlaceConfig arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, PUZZLE_ENTERED,
                               new Object[] { arg1 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case PUZZLE_ENTERED:
                ((EnterPuzzleListener)listener).puzzleEntered(
                    (PlaceConfig)args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

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

    /** The method id used to dispatch {@link #enterPuzzle} requests. */
    public static final int ENTER_PUZZLE = 2;

    // documentation inherited from interface
    public void enterPuzzle (Client arg1, int arg2, EnterPuzzleListener arg3)
    {
        EnterPuzzleMarshaller listener3 = new EnterPuzzleMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, ENTER_PUZZLE, new Object[] {
            new Integer(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #leavePuzzle} requests. */
    public static final int LEAVE_PUZZLE = 3;

    // documentation inherited from interface
    public void leavePuzzle (Client arg1)
    {
        sendRequest(arg1, LEAVE_PUZZLE, new Object[] {
            
        });
    }

}
