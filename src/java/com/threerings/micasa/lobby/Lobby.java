//
// $Id: Lobby.java,v 1.6 2004/08/27 02:12:50 mdb Exp $
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

package com.threerings.micasa.lobby;

import com.threerings.io.TrackedStreamableObject;

/**
 * A simple class for keeping track of information for each lobby in
 * operation on the server.
 */
public class Lobby extends TrackedStreamableObject
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
