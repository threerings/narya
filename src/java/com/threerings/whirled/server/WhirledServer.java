//
// $Id: WhirledServer.java,v 1.1 2001/08/11 04:09:50 mdb Exp $

package com.threerings.whirled.server;

import java.io.IOException;

import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.whirled.Log;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.server.persist.DummySceneRepository;

/**
 * The whirled server extends the party server and provides access to
 * managers and the like that are needed by the whirled serviecs.
 */
public class WhirledServer extends PartyServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "whirled";

    /** The scene registry. */
    public static SceneRegistry screg;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws IOException
    {
        // do the cher server initialization
        super.init();

        // bind the whirled server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH, true);

        // instantiate the scene repository
        SceneRepository screp = null;
        try {
            screp = (SceneRepository)config.instantiateValue(
                SCENEREP_KEY, DEF_SCENEREP);
        } catch (Exception e) {
            Log.warning("Unable to instantiate scene repository " +
                        "[error=" + e + "].");
            throw new IOException("Fatal init failure");
        }

        // create our scene registry
        screg = new SceneRegistry(screp);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));

        Log.info("Whirled server initialized.");
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

    // the path to the config file
    protected final static String CONFIG_PATH =
        "rsrc/config/whirled/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";

    // scene repository related configuration info
    protected final static String SCENEREP_KEY = "scene_rep";
    protected final static String DEF_SCENEREP =
        DummySceneRepository.class.getName();
}
