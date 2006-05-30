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

package com.threerings.micasa.server;

import com.threerings.crowd.server.CrowdServer;
import com.threerings.parlor.server.ParlorManager;

import com.threerings.micasa.Log;
import com.threerings.micasa.lobby.LobbyRegistry;

/**
 * This class is the main entry point and general organizer of everything
 * that goes on in the MiCasa game server process.
 */
public class MiCasaServer extends CrowdServer
{
    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

    /** The lobby registry operating on this server. */
    public static LobbyRegistry lobreg = new LobbyRegistry();

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the base server initialization
        super.init();

        // configure the client manager to use our client class
        clmgr.setClientClass(MiCasaClient.class);

        // initialize our parlor manager
        parmgr.init(invmgr, plreg);

        // initialize the lobby registry
        lobreg.init(invmgr);

        Log.info("MiCasa server initialized.");
    }

    public static void main (String[] args)
    {
        MiCasaServer server = new MiCasaServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }
}
