//
// $Id: MiCasaServer.java,v 1.6 2002/08/14 19:07:49 mdb Exp $

package com.threerings.micasa.server;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;

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
    /** The database connection provider in use by this server. */
    public static ConnectionProvider conprov;

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

        // create our connection provider
        String dbmap = MiCasaConfig.config.getValue(DBMAP_KEY, DEF_DBMAP);
        conprov = new StaticConnectionProvider(dbmap);

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

    // connection provider related configuration info
    protected final static String DBMAP_KEY = "dbmap";
    protected final static String DEF_DBMAP =
        "rsrc/config/micasa/dbmap.properties";
}
