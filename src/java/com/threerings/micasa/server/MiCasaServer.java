//
// $Id: MiCasaServer.java,v 1.1 2001/10/03 23:24:09 mdb Exp $

package com.threerings.micasa.server;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.Config;

import com.threerings.cocktail.party.server.PartyServer;
import com.threerings.parlor.server.ParlorManager;

import com.threerings.micasa.Log;

/**
 * This class is the main entry point and general organizer of everything
 * that goes on in the MiCasa game server process.
 */
public class MiCasaServer extends PartyServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "micasa";

    /** The database connection provider in use by this server. */
    public static ConnectionProvider conprov;

    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the cher server initialization
        super.init();

        // bind the whirled server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH, true);

        // initialize our parlor manager
        parmgr.init(config, invmgr);

        // create our connection provider
        String dbmap = config.getValue(DBMAP_KEY, DEF_DBMAP);
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

    // the path to the config file
    protected final static String CONFIG_PATH =
        "rsrc/config/micasa/server";

    // connection provider related configuration info
    protected final static String DBMAP_KEY = CONFIG_KEY + ".dbmap";
    protected final static String DEF_DBMAP =
        "rsrc/config/micasa/dbmap.properties";
}
