//
// $Id: Lobby.java,v 1.4 2002/07/23 05:54:52 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.io.SimpleStreamableObject;

/**
 * A simple class for keeping track of information for each lobby in
 * operation on the server.
 */
public class Lobby extends SimpleStreamableObject
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
}
