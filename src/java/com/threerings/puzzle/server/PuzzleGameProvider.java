//
// $Id: PuzzleGameProvider.java,v 1.2 2004/08/27 02:20:32 mdb Exp $
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
