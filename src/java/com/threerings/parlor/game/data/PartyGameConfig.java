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

package com.threerings.parlor.game.data;

/**
 * Provides additional information for party games.
 * A party game is a game in which the players are not set prior to starting,
 * but players can come and go at will during the normal progression of
 * the game.
 */
public interface PartyGameConfig
{
    /** Party game constant indicating that this game, while it does
     * implement PartyGameConfig, is not currently being played in party
     * mode. */
    public static final byte NOT_PARTY_GAME = 0;

    /** Party game constant indicating that we're in a party game in which
     * players must sit at an available seat to play, otherwise they're
     * an observer. */
    public static final byte SEATED_PARTY_GAME = 1;

    /** Party game constant indicating that everyone in the game place is
     * a "player", meaning that they do not need to claim a seat to play. */
    public static final byte FREE_FOR_ALL_PARTY_GAME = 2;

    /**
     * Get the type of party game being played.
     */
    public byte getPartyGameType ();
}
