//
// $Id$
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
