//
// $Id: PuzzleGameDispatcher.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.puzzle.client.PuzzleGameService;
import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.PuzzleGameMarshaller;

/**
 * Dispatches requests to the {@link PuzzleGameProvider}.
 */
public class PuzzleGameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public PuzzleGameDispatcher (PuzzleGameProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new PuzzleGameMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case PuzzleGameMarshaller.UPDATE_PROGRESS:
            ((PuzzleGameProvider)provider).updateProgress(
                source,
                ((Integer)args[0]).intValue(), (int[])args[1]
            );
            return;

        case PuzzleGameMarshaller.UPDATE_PROGRESS_SYNC:
            ((PuzzleGameProvider)provider).updateProgressSync(
                source,
                ((Integer)args[0]).intValue(), (int[])args[1], (Board[])args[2]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
