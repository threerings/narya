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

/**
 * A game config object that is to be matchmade using the table services
 * should implement this interface so that the table services can extract
 * the necessary table-generic information.
 */
public interface TableConfig
{
    /**
     * Returns the minimum number of players needed to start the game.
     */
    public int getMinimumPlayers ();

    /**
     * Returns the maximum number of players that can play in the game.
     */
    public int getMaximumPlayers ();

    /**
     * Returns the number of players that when reached, will cause the
     * game to automatically be started. If this value is -1 the game will
     * not automatically be started until the maximum number of players is
     * reached.
     */
    public int getDesiredPlayers ();

    /**
     * Sets the desired number of players to the specified value.
     */
    public void setDesiredPlayers (int desiredPlayers);
    
    /**
     * Checks whether or not this is a configuration for a private table.
     */
    public boolean isPrivateTable ();
    
    /**
     * Sets whether nor not this is a configuration for a private table.
     */
    public void setPrivateTable (boolean privateTable);
}
