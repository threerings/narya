//
// $Id: PuzzleCodes.java,v 1.4 2004/10/21 02:54:44 mdb Exp $
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

import com.threerings.presents.data.InvocationCodes;
import com.threerings.util.MessageBundle;

/**
 * Constants relating to the puzzle services.
 */
public interface PuzzleCodes extends InvocationCodes
{
    /** The message bundle identifier for general puzzle messages. */
    public static final String PUZZLE_MESSAGE_BUNDLE = "puzzle.general";

    /** The ID for puzzle chat. */
    public static final String PUZZLE_CHAT_TYPE = "puzzleChat";

    /** The default puzzle difficulty level. */
    public static final int DEFAULT_DIFFICULTY = 2;

    /** The name of the message event to a placeObject that a player
     * was knocked out of a puzzle. */
    public static final String PLAYER_KNOCKED_OUT = "playerKnocked";

    /** The name of the message event to a placeObject that reports
     * the winners and losers of a game. */
    public static final String WINNERS_AND_LOSERS = "winnersAndLosers";

    /** Enable this flag to have the client send a copy of its board state
     * with every event to the server for checking. */
    public static final boolean SYNC_BOARD_STATE = true;

    /** Whether to enable debug logging and assertions for puzzles. Note
     * that enabling this may result in the server or client exiting
     * unexpectedly if certain error conditions arise in order to
     * facilitate debugging, and so this should never be enabled in any
     * environment even remotely resembling production. */
    public static final boolean DEBUG_PUZZLE = false;
}
