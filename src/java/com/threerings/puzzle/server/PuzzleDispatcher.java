//
// $Id: PuzzleDispatcher.java,v 1.4 2004/08/27 02:20:32 mdb Exp $
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

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.puzzle.client.PuzzleService;
import com.threerings.puzzle.client.PuzzleService.EnterPuzzleListener;
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

        case PuzzleMarshaller.ENTER_PUZZLE:
            ((PuzzleProvider)provider).enterPuzzle(
                source,
                ((Integer)args[0]).intValue(), (EnterPuzzleListener)args[1]
            );
            return;

        case PuzzleMarshaller.LEAVE_PUZZLE:
            ((PuzzleProvider)provider).leavePuzzle(
                source
                
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
