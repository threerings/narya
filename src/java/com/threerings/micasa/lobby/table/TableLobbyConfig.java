//
// $Id: TableLobbyConfig.java,v 1.1 2001/10/23 20:24:10 mdb Exp $

package com.threerings.micasa.lobby.table;

import javax.swing.JComponent;
import com.threerings.micasa.lobby.LobbyConfig;
import com.threerings.micasa.util.MiCasaContext;

/**
 * Instructs the lobby services to use a {@link TableListView} as the
 * matchmaking component.
 */
public class TableLobbyConfig extends LobbyConfig
{
    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.micasa.lobby.table.TableLobbyManager";
    }

    // documentation inherited
    public JComponent createMatchMakingView (MiCasaContext ctx)
    {
        return new TableListView(ctx, this);
    }
}
