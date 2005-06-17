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

package com.threerings.parlor.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Table configuration parameters for a game that is to be matchmade
 * using the table services.
 */
public class TableConfig extends SimpleStreamableObject
{
    /** The total number of players that are desired for the table,
     * or -1 for a party game. For team games, this should be set to the
     * total number of players overall, as teams may be unequal. */
    public int desiredPlayerCount;

    /** The minimum number of players needed overall (or per-team if a
     * team-based game) for the game to start at the creator's discretion. */
    public int minimumPlayerCount;

    /** If non-null, indicates that this is a team-based game and contains
     * the team assignments for each player. For example, a game with
     * three players in two teams- players 0 and 2 versus player 1- would
     * have { {0, 2}, {1} }; */
    public int[][] teamMemberIndices;

    /** Whether the table is "private". */
    public boolean privateTable;
}
