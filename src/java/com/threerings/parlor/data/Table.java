//
// $Id: Table.java,v 1.1 2001/10/19 02:04:29 mdb Exp $

package com.threerings.parlor.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.io.ValueMarshaller;

import com.threerings.parlor.game.GameConfig;

/**
 * This class represents a table that is being used to matchmake a game by
 * the Parlor services.
 */
public class Table implements DSet.Element
{
    /** The unique identifier for this table. */
    public Integer tableId;

    /** An array of the usernames of the occupants of this table (some
     * slots may not be filled. */
    public String[] occupants;

    /** The game config for the game that is being matchmade. This config
     * instance will also implement {@link TableConfig}. */
    public GameConfig config;

    /**
     * A convenience function for accessing the table id as an int.
     */
    public int getTableId ()
    {
        return tableId.intValue();
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
        ValueMarshaller.writeTo(out, occupants);
        ValueMarshaller.writeTo(out, config);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        tableId = new Integer(in.readInt());
        occupants = (String[])ValueMarshaller.readFrom(in);
        config = (GameConfig)ValueMarshaller.readFrom(in);
    }

    /**
     * Generates a string representation of this table instance.
     */
    public String toString ()
    {
        return "[tableId=" + tableId +
            ", occupants=" + StringUtil.toString(occupants) +
            ", config=" + config + "]";
    }
}
