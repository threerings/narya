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

import java.io.IOException;

import com.threerings.io.ObjectInputStream;

import com.samskivert.util.StringUtil;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.GameConfig;

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
     * slots may not be filled). */
    public Name[] occupants;

    /** The body oids of the occupants of this table. (This is not
     * propagated to remote instances.) */
    public transient int[] bodyOids;

    /** The game config for the game that is being matchmade. This config
     * instance will also implement {@link TableConfig}. */
    public GameConfig config;

    /**
     * Creates a new table instance, and assigns it the next monotonically
     * increasing table id. The supplied config instance must implement
     * {@link TableConfig} or a <code>ClassCastException</code> will be
     * thrown.
     *
     * @param lobbyOid the object id of the lobby in which this table is
     * to live.
     * @param config the configuration of the game being matchmade by this
     * table.
     */
    public Table (int lobbyOid, GameConfig config)
    {
        // assign a unique table id
        tableId = new Integer(++_tableIdCounter);

        // keep track of our lobby oid
        this.lobbyOid = lobbyOid;

        // keep a casted reference around
        _tconfig = (TableConfig)config;
        this.config = config;

        // make room for the maximum number of players
        occupants = new Name[_tconfig.getMaximumPlayers()];
        bodyOids = new int[occupants.length];
    }

    /**
     * Constructs a blank table instance, suitable for unserialization.
     */
    public Table ()
    {
    }

    /**
     * Extends default behavior to initialize transient members.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        
        // initialize casted reference
        _tconfig = (TableConfig)config;
    }
    
    /**
     * A convenience function for accessing the table id as an int.
     */
    public int getTableId ()
    {
        return tableId.intValue();
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
        // count up the players
        int pcount = 0;
        for (int i = 0; i < occupants.length; i++) {
            if (occupants[i] != null) {
                pcount++;
            }
        }

        // create and populate the players array
        Name[] players = new Name[pcount];
        pcount = 0;
        for (int i = 0; i < occupants.length; i++) {
            if (occupants[i] != null) {
                players[pcount++] = occupants[i];
            }
        }

        return players;
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
        // find out how many positions we have for occupation
        int maxpos = _tconfig.getDesiredPlayers();
        // if there is no desired number of players, use the max
        if (maxpos == -1) {
            maxpos = _tconfig.getMaximumPlayers();
        }

        // make sure the requested position is a valid one
        if (position >= maxpos) {
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
                occupants[i] = null;
                bodyOids[i] = 0;
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
                occupants[i] = null;
                bodyOids[i] = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this table has a sufficient number of occupants
     * that the game can be started.
     */
    public boolean mayBeStarted ()
    {
        // make sure at least the minimum number of players are here
        int want = _tconfig.getMinimumPlayers(), have = 0;
        for (int i = 0; i < occupants.length; i++) {
            if (occupants[i] != null) {
                if (++have == want) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if sufficient seats are occupied that the game should
     * be automatically started.
     */
    public boolean shouldBeStarted ()
    {
        int need = _tconfig.getDesiredPlayers();
        if (need == -1) {
            need = _tconfig.getMaximumPlayers();
        }
        for (int i = 0; i < occupants.length; i++) {
            if (occupants[i] != null && --need == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there is no one sitting at this table.
     */
    public boolean isEmpty ()
    {
        for (int i = 0; i < occupants.length; i++) {
            if (occupants[i] != null) {
                return false;
            }
        }
        return true;
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

    /** A casted reference of our game config object. */
    protected transient TableConfig _tconfig;

    /** A counter for assigning table ids. */
    protected static int _tableIdCounter = 0;
}
