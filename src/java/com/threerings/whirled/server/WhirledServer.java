//
// $Id: WhirledServer.java,v 1.6 2001/10/05 23:59:36 mdb Exp $

package com.threerings.whirled.server;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.Config;

import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.whirled.Log;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.test.DummySceneRepository;

/**
 * The whirled server extends the party server and provides access to
 * managers and the like that are needed by the whirled serviecs.
 */
public class WhirledServer extends PartyServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "whirled";

    /** The database connection provider in use by this server. */
    public static ConnectionProvider conprov;

    /** The scene registry. */
    public static SceneRegistry screg;

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

        // create our connection provider
        conprov = createConnectionProvider(config);

        // create the scene repository
        _screp = createSceneRepository(conprov);

        // create our scene registry
        screg = new SceneRegistry(_screp);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));

        Log.info("Whirled server initialized.");
    }

    /**
     * Creates the connection provider that will be used by this server.
     * If a derived class wishes to use a particular kind of connection
     * provider, it can override this method. The default mechanism is to
     * load a properties file referenced by <code>dbmap</code> in the
     * whirled server configuration and use those properties to create a
     * {@link StaticConnectionProvider}.
     *
     * @exception Exception thrown if an error occurs creating the
     * connection provider.
     */
    protected ConnectionProvider createConnectionProvider (Config config)
        throws Exception
    {
        String dbmap = config.getValue(DBMAP_KEY, DEF_DBMAP);
        return new StaticConnectionProvider(dbmap);
    }

    /**
     * Creates the scene repository that will be used by this server. If a
     * derived class wishes to use a particular kind of scene repository
     * (which they most likely will), they should override this method and
     * instantiate the scene repository of their choosing.
     *
     * @exception Exception thrown if any error occurs while instantiating
     * or initializing the scene repository.
     */
    protected SceneRepository createSceneRepository (
        ConnectionProvider conprov)
        throws Exception
    {
        return new DummySceneRepository();
    }

    public static void main (String[] args)
    {
        WhirledServer server = new WhirledServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    /** The scene repository in use by this server. */
    protected SceneRepository _screp;

    // the path to the config file
    protected final static String CONFIG_PATH =
        "rsrc/config/whirled/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";

    // connection provider related configuration info
    protected final static String DBMAP_KEY = CONFIG_KEY + ".dbmap";
    protected final static String DEF_DBMAP =
        "rsrc/config/whirled/dbmap";
}
