//
// $Id: PartyGameConfig.java,v 1.2 2004/08/27 02:20:14 mdb Exp $
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

package com.threerings.parlor.game;

import com.threerings.parlor.data.TableConfig;

/**
 * Provides additional information for party games.
 */
public interface PartyGameConfig extends TableConfig
{
    /**
     * Returns true if this party game is being played in party game mode,
     * false if it is not.
     */
    public boolean isPartyGame ();

    /**
     * Configures this game config as a party game or not.
     */
    public void setPartyGame (boolean isPartyGame);

    /**
     * Returns an array of strings that describe the configuration of this
     * party game. This should eventually be rolled into a more general
     * purpose mechanism for generating descriptions of game
     * configurations as well as editors for game configurations (which
     * already exists in rudimentary form).
     */
    public String[] getPartyDescription ();
}
