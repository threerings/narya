//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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
