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
