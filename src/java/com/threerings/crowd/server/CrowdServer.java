//
// $Id: CrowdServer.java,v 1.5 2001/08/04 01:55:41 mdb Exp $

package com.threerings.cocktail.party.server;

import java.io.IOException;
import java.util.HashMap;

import com.threerings.cocktail.cher.server.CherServer;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.BodyObject;

/**
 * The party server extends the cher server by configuring it to use the
 * extensions provided by the party layer to support party services.
 */
public class PartyServer extends CherServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "party";

    /** The place registry. */
    public static PlaceRegistry plreg;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws IOException
    {
        // do the cher server initialization
        super.init();

        // bind the party server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH);

        // configure the client to use our party client
        clmgr.setClientClass(PartyClient.class);

        // configure the client to use the body object
        clmgr.setClientObjectClass(BodyObject.class);

        // create our place registry
        plreg = new PlaceRegistry(config);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));

        Log.info("Party server initialized.");
    }

    /**
     * The party server maintains a mapping of username to body object for
     * all active users on the server. This should only be called from the
     * dobjmgr thread.
     */
    public static BodyObject lookupBody (String username)
    {
        return (BodyObject)_bodymap.get(username);
    }

    /**
     * Called by the party client to map a username to a particular body
     * object. This should only be called from the dobjmgr thread.
     */
    protected static void mapBody (String username, BodyObject bodobj)
    {
        _bodymap.put(username, bodobj);
    }

    /**
     * Called by the party client to unmap a username from a particular
     * body object. This should only be called from the dobjmgr thread.
     */
    protected static void unmapBody (String username)
    {
        _bodymap.remove(username);
    }

    public static void main (String[] args)
    {
        PartyServer server = new PartyServer();
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
        "rsrc/config/cocktail/party/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";
}
