//
// $Id: PuzzleGameMarshaller.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.puzzle.client.PuzzleGameService;
import com.threerings.puzzle.data.Board;

/**
 * Provides the implementation of the {@link PuzzleGameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class PuzzleGameMarshaller extends InvocationMarshaller
    implements PuzzleGameService
{
    /** The method id used to dispatch {@link #updateProgress} requests. */
    public static final int UPDATE_PROGRESS = 1;

    // documentation inherited from interface
    public void updateProgress (Client arg1, int arg2, int[] arg3)
    {
        sendRequest(arg1, UPDATE_PROGRESS, new Object[] {
            new Integer(arg2), arg3
        });
    }

    /** The method id used to dispatch {@link #updateProgressSync} requests. */
    public static final int UPDATE_PROGRESS_SYNC = 2;

    // documentation inherited from interface
    public void updateProgressSync (Client arg1, int arg2, int[] arg3, Board[] arg4)
    {
        sendRequest(arg1, UPDATE_PROGRESS_SYNC, new Object[] {
            new Integer(arg2), arg3, arg4
        });
    }

}
