//
// $Id: CrowdServer.java,v 1.10 2002/03/05 05:33:25 mdb Exp $

package com.threerings.crowd.server;

import java.util.HashMap;

import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;

/**
 * The crowd server extends the presents server by configuring it to use the
 * extensions provided by the crowd layer to support crowd services.
 */
public class CrowdServer extends PresentsServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "crowd";

    /** The place registry. */
    public static PlaceRegistry plreg;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the presents server initialization
        super.init();

        // bind the crowd server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH, true);

        // configure the client manager to use our client
        clmgr.setClientClass(CrowdClient.class);

        // configure the client manager to use our resolver
        clmgr.setClientResolverClass(CrowdClientResolver.class);

        // create our place registry
        plreg = new PlaceRegistry(config, invmgr, omgr);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));

        Log.info("Crowd server initialized.");
    }

    /**
     * The server maintains a mapping of username to body object for all
     * active users on the server. This should only be called from the
     * dobjmgr thread.
     */
    public static BodyObject lookupBody (String username)
    {
        return (BodyObject)clmgr.getClientObject(username);
    }

    public static void main (String[] args)
    {
        CrowdServer server = new CrowdServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    /** We use this to map usernames to body objects. */
    protected static HashMap _bodymap = new HashMap();

    // the path to the config file
    protected final static String CONFIG_PATH =
        "rsrc/config/crowd/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";
}
