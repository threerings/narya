//
// $Id: PuzzleDispatcher.java,v 1.5 2004/10/21 02:54:44 mdb Exp $

package com.threerings.puzzle.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.puzzle.client.PuzzleService;
import com.threerings.puzzle.data.PuzzleMarshaller;
import com.threerings.puzzle.data.SolitairePuzzleConfig;

/**
 * Dispatches requests to the {@link PuzzleProvider}.
 */
public class PuzzleDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public PuzzleDispatcher (PuzzleProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new PuzzleMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case PuzzleMarshaller.START_PUZZLE:
            ((PuzzleProvider)provider).startPuzzle(
                source,
                (SolitairePuzzleConfig)args[0], (ConfirmListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
