//
// $Id: CrowdServer.java,v 1.8 2001/10/11 04:07:51 mdb Exp $

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

        // configure the client to use our crowd client
        clmgr.setClientClass(CrowdClient.class);

        // configure the client to use the body object
        clmgr.setClientObjectClass(BodyObject.class);

        // create our place registry
        plreg = new PlaceRegistry(config);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));

        Log.info("Crowd server initialized.");
    }

    /**
     * The crowd server maintains a mapping of username to body object for
     * all active users on the server. This should only be called from the
     * dobjmgr thread.
     */
    public static BodyObject lookupBody (String username)
    {
        return (BodyObject)_bodymap.get(username);
    }

    /**
     * Called by the crowd client to map a username to a particular body
     * object. This should only be called from the dobjmgr thread.
     */
    protected static void mapBody (String username, BodyObject bodobj)
    {
        _bodymap.put(username, bodobj);
    }

    /**
     * Called by the crowd client to unmap a username from a particular
     * body object. This should only be called from the dobjmgr thread.
     */
    protected static void unmapBody (String username)
    {
        _bodymap.remove(username);
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
