//
// $Id: MiCasaServer.java,v 1.8 2003/04/01 04:00:54 mdb Exp $

package com.threerings.micasa.server;

import com.threerings.crowd.server.CrowdServer;
import com.threerings.parlor.server.ParlorManager;
import com.threerings.presents.client.Client;

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

    /**
     * Returns the port on which the connection manager will listen for
     * client connections.
     */
    protected int getListenPort ()
    {
        int port = Client.DEFAULT_SERVER_PORT;
        try {
            port = Integer.parseInt(System.getProperty("port"));
        } catch (Exception e) {
        }
        return port;
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
