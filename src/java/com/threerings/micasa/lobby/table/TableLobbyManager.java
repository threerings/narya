//
// $Id: TableLobbyManager.java,v 1.1 2001/10/23 20:24:10 mdb Exp $

package com.threerings.micasa.lobby.table;

import com.threerings.micasa.lobby.LobbyManager;

/**
 * Extends lobby manager only to ensure that a table lobby object is used
 * for table lobbies.
 */
public class TableLobbyManager extends LobbyManager
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return TableLobbyObject.class;
    }
}
