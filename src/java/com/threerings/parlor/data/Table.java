//
// $Id: Table.java,v 1.4 2001/10/23 02:22:16 mdb Exp $

package com.threerings.parlor.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.io.ValueMarshaller;

import com.threerings.parlor.client.ParlorCodes;
import com.threerings.parlor.game.GameConfig;

/**
 * This class represents a table that is being used to matchmake a game by
 * the Parlor services.
 */
public class Table
    implements DSet.Element, ParlorCodes
{
    /** The unique identifier for this table. */
    public Integer tableId;

    /** The object id of the lobby object with which this table is
     * associated. */
    public int lobbyOid;

    /** The oid of the game that was created from this table or -1 if the
     * table is still in matchmaking mode. */
    public int gameOid;

    /** An array of the usernames of the occupants of this table (some
     * slots may not be filled. */
    public String[] occupants;

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
        occupants = new String[_tconfig.getMaximumPlayers()];
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
     * Once a table is ready to play (see {@link #readyToStart}), the
     * players array can be fetched using this method. It will return an
     * array containing the usernames of all of the players in the game,
     * sized properly and with each player in the appropriate position.
     */
    public String[] getPlayers ()
    {
        // count up the players
        int pcount = 0;
        for (int i = 0; i < occupants.length; i++) {
            if (!StringUtil.blank(occupants[i])) {
                pcount++;
            }
        }

        // create and populate the players array
        String[] players = new String[pcount];
        pcount = 0;
        for (int i = 0; i < occupants.length; i++) {
            if (!StringUtil.blank(occupants[i])) {
                players[pcount++] = occupants[i];
            }
        }

        return players;
    }

    /**
     * Requests to seat the specified user at the specified position in
     * this table.
     *
     * @return null if the user was successfully seated, a string error
     * code explaining the failure if the user was not able to be seated
     * at that position.
     */
    public String setOccupant (int position, String username)
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
        if (!StringUtil.blank(occupants[position])) {
            return TABLE_POSITION_OCCUPIED;
        }

        // otherwise all is well, stick 'em in
        occupants[position] = username;
        return null;
    }

    /**
     * Returns true if this table has occupants in all of the desired
     * positions and should be started.
     */
    public boolean readyToStart ()
    {
        return false;
    }

    // documentation inherited
    public Object getKey ()
    {
        return tableId;
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(getTableId());
        out.writeInt(lobbyOid);
        out.writeInt(gameOid);
        ValueMarshaller.writeTo(out, occupants);
        ValueMarshaller.writeTo(out, config);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        tableId = new Integer(in.readInt());
        gameOid = in.readInt();
        lobbyOid = in.readInt();
        occupants = (String[])ValueMarshaller.readFrom(in);
        config = (GameConfig)ValueMarshaller.readFrom(in);
        _tconfig = (TableConfig)config;
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
            ", config=" + config + "]";
    }

    /** A casted reference of our game config object. */
    protected TableConfig _tconfig;

    /** A counter for assigning table ids. */
    protected static int _tableIdCounter = 0;
}
