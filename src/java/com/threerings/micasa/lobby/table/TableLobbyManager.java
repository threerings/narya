//
// $Id: TableLobbyManager.java,v 1.2 2001/10/23 23:47:33 mdb Exp $

package com.threerings.micasa.lobby.table;

import com.threerings.parlor.server.TableManager;
import com.threerings.parlor.server.TableManagerProvider;
import com.threerings.micasa.lobby.LobbyManager;

/**
 * Extends lobby manager only to ensure that a table lobby object is used
 * for table lobbies.
 */
public class TableLobbyManager
    extends LobbyManager implements TableManagerProvider
{
    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();

        // now that we have our place object, we can create our table
        // manager
        _tmgr = new TableManager(this);
    }

    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return TableLobbyObject.class;
    }

    // documentation inherited
    public TableManager getTableManager ()
    {
        return _tmgr;
    }

    /** A reference to our table manager. */
    protected TableManager _tmgr;
}
