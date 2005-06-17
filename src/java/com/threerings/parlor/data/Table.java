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

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.data.GameConfig;

/**
 * This class represents a table that is being used to matchmake a game by
 * the Parlor services.
 */
public class Table
    implements DSet.Entry, ParlorCodes
{
    /** The unique identifier for this table. */
    public Integer tableId;

    /** The object id of the lobby object with which this table is
     * associated. */
    public int lobbyOid;

    /** The oid of the game that was created from this table or -1 if the
     * table is still in matchmaking mode. */
    public int gameOid = -1;

    /** An array of the usernames of the occupants of this table (some
     * slots may not be filled), or null if a party game. */
    public Name[] occupants;

    /** The body oids of the occupants of this table, or null if a party game.
     * (This is not propagated to remote instances.) */
    public transient int[] bodyOids;

    /** The game config for the game that is being matchmade. */
    public GameConfig config;

    /** The table configuration object. */
    public TableConfig tconfig;

    /**
     * Creates a new table instance, and assigns it the next monotonically
     * increasing table id.
     *
     * @param lobbyOid the object id of the lobby in which this table is
     * to live.
     * @param tconfig the table configuration for this table.
     * @param config the configuration of the game being matchmade by this
     * table.
     */
    public Table (int lobbyOid, TableConfig tconfig, GameConfig config)
    {
        // assign a unique table id
        tableId = new Integer(++_tableIdCounter);

        // keep track of our lobby oid
        this.lobbyOid = lobbyOid;

        // keep a casted reference around
        this.tconfig = tconfig;
        this.config = config;

        // make room for the maximum number of players
        if (tconfig.desiredPlayerCount != -1) {
            occupants = new Name[tconfig.desiredPlayerCount];
            bodyOids = new int[occupants.length];

            // fill in information on the AIs
            int acount = (config.ais == null) ? 0 : config.ais.length;
            for (int ii = 0; ii < acount; ii++) {
                // TODO: handle this naming business better
                occupants[ii] = new Name("AI " + (ii+1));
            }
        }
    }

    /**
     * Constructs a blank table instance, suitable for unserialization.
     */
    public Table ()
    {
    }
    
    /**
     * A convenience function for accessing the table id as an int.
     */
    public int getTableId ()
    {
        return tableId.intValue();
    }

    /**
     * Returns true if there is no one sitting at this table.
     */
    public boolean isEmpty ()
    {
        for (int i = 0; i < bodyOids.length; i++) {
            if (bodyOids[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Count the number of players currently occupying this table.
     */
    public int getOccupiedCount ()
    {
        int count = 0;
        for (int ii = 0; ii < occupants.length; ii++) {
            if (occupants[ii] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Once a table is ready to play (see {@link #mayBeStarted} and {@link
     * #shouldBeStarted}), the players array can be fetched using this
     * method. It will return an array containing the usernames of all of
     * the players in the game, sized properly and with each player in the
     * appropriate position.
     */
    public Name[] getPlayers ()
    {
        if (occupants == null) {
            return null;
        }

        // create and populate the players array
        Name[] players = new Name[getOccupiedCount()];
        for (int ii = 0, dex = 0; ii < occupants.length; ii++) {
            if (occupants[ii] != null) {
                players[dex++] = occupants[ii];
            }
        }

        return players;
    }

    /**
     * For a team game, get the team member indices of the compressed
     * players array returned by getPlayers().
     */
    public int[][] getTeamMemberIndices ()
    {
        int[][] teams = tconfig.teamMemberIndices;
        if (teams == null) {
            return null;
        }

        // compress the team indexes down
        ArrayIntSet set = new ArrayIntSet();
        int[][] newTeams = new int[teams.length][];
        Name[] players = getPlayers();
        for (int ii=0; ii < teams.length; ii++) {
            set.clear();
            for (int jj=0; jj < teams[ii].length; jj++) {
                Name occ = occupants[teams[ii][jj]];
                if (occ != null) {
                    set.add(ListUtil.indexOf(players, occ));
                }
            }
            newTeams[ii] = set.toIntArray();
        }

        return newTeams;
    }

    /**
     * Requests to seat the specified user at the specified position in
     * this table.
     *
     * @param position the position in which to seat the user.
     * @param username the username of the user to be set.
     * @param bodyOid the body object id of the user to be set.
     *
     * @return null if the user was successfully seated, a string error
     * code explaining the failure if the user was not able to be seated
     * at that position.
     */
    public String setOccupant (int position, Name username, int bodyOid)
    {
        // make sure the requested position is a valid one
        if (position >= tconfig.desiredPlayerCount || position < 0) {
            return INVALID_TABLE_POSITION;
        }

        // make sure the requested position is not already occupied
        if (occupants[position] != null) {
            return TABLE_POSITION_OCCUPIED;
        }

        // otherwise all is well, stick 'em in
        occupants[position] = username;
        bodyOids[position] = bodyOid;
        return null;
    }

    /**
     * Requests that the specified user be removed from their seat at this
     * table.
     *
     * @return true if the user was seated at the table and has now been
     * removed, false if the user was never seated at the table in the
     * first place.
     */
    public boolean clearOccupant (Name username)
    {
        for (int i = 0; i < occupants.length; i++) {
            if (username.equals(occupants[i])) {
                clearOccupantPos(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Requests that the user identified by the specified body object id
     * be removed from their seat at this table.
     *
     * @return true if the user was seated at the table and has now been
     * removed, false if the user was never seated at the table in the
     * first place.
     */
    public boolean clearOccupant (int bodyOid)
    {
        for (int i = 0; i < bodyOids.length; i++) {
            if (bodyOid == bodyOids[i]) {
                clearOccupantPos(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Internal method used for clearing an occupant once we've located
     * the right position.
     */
    protected void clearOccupantPos (int position)
    {
        occupants[position] = null;
        bodyOids[position] = 0;
    }

    /**
     * Returns true if this table has a sufficient number of occupants
     * that the game can be started.
     */
    public boolean mayBeStarted ()
    {
        if (tconfig.teamMemberIndices == null) {
            // for a normal game, just check to see if we're past the minimum
            return tconfig.minimumPlayerCount <= getOccupiedCount();

        } else {
            // for a team game, make sure each team has the minimum players
            int[][] teams = tconfig.teamMemberIndices;
            for (int ii=0; ii < teams.length; ii++) {
                int teamCount = 0;
                for (int jj=0; jj < teams[ii].length; jj++) {
                    if (occupants[teams[ii][jj]] != null) {
                        teamCount++;
                    }
                }
                if (teamCount < tconfig.minimumPlayerCount) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Returns true if sufficient seats are occupied that the game should
     * be automatically started.
     */
    public boolean shouldBeStarted ()
    {
        return tconfig.desiredPlayerCount <= getOccupiedCount();
    }

    /**
     * Returns true if this table is in play, false if it is still being
     * matchmade.
     */
    public boolean inPlay ()
    {
        return gameOid != -1;
    }

    // documentation inherited
    public Comparable getKey ()
    {
        return tableId;
    }

    /**
     * Returns true if this table is equal to the supplied object (which
     * must be a table with the same table id).
     */
    public boolean equals (Object other)
    {
        if (other != null && other instanceof Table) {
            return ((Table)other).tableId.equals(tableId);
        } else {
            return false;
        }
    }

    /**
     * Generates a string representation of this table instance.
     */
    public String toString ()
    {
        return "[tableId=" + tableId +
            ", lobbyOid=" + lobbyOid +
            ", gameOid=" + gameOid +
            ", occupants=" + StringUtil.toString(occupants) +
            ", bodyOids=" + StringUtil.toString(bodyOids) +
            ", config=" + config + "]";
    }

    /** A counter for assigning table ids. */
    protected static int _tableIdCounter = 0;
}
