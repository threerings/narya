//
// $Id: PuzzleMarshaller.java,v 1.4 2004/08/27 02:20:28 mdb Exp $
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
