//
// $Id: Lobby.java,v 1.3 2002/02/01 23:43:45 mdb Exp $

package com.threerings.micasa.lobby;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.io.Marshaller;
import com.threerings.presents.io.Streamable;

/**
 * A simple class for keeping track of information for each lobby in
 * operation on the server.
 */
public class Lobby implements Streamable
{
    /** The object id of the lobby place object. */
    public int placeOid;

    /** The universal game identifier string for the game matchmade by
     * this lobby. */
    public String gameIdent;

    /** The human readable name of the lobby. */
    public String name;

    /**
     * Constructs a lobby record and initializes it with the specified
     * values.
     */
    public Lobby (int placeOid, String gameIdent, String name)
    {
        this.placeOid = placeOid;
        this.gameIdent = gameIdent;
        this.name = name;
    }

    /**
     * Constructs a blank lobby record suitable for unserialization.
     */
    public Lobby ()
    {
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        Marshaller.writeObject(out, this);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        Marshaller.readObject(in, this);
    }

    public String toString ()
    {
        return "[oid=" + placeOid + ", ident=" + gameIdent +
            ", name=" + name + "]";
    }
}
