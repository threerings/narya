//
// $Id: CrowdServer.java,v 1.12 2002/04/18 00:44:50 shaper Exp $

package com.threerings.crowd.server;

import java.util.Iterator;

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

        // configure the client manager to use our client
        clmgr.setClientClass(CrowdClient.class);

        // configure the client manager to use our resolver
        clmgr.setClientResolverClass(CrowdClientResolver.class);

        // create our place registry
        plreg = new PlaceRegistry(invmgr, omgr);

        // register our invocation service providers
        String[] providers = null;
        providers = CrowdConfig.config.getValue(PROVIDERS_KEY, providers);
        registerProviders(providers);

        Log.info("Crowd server initialized.");
    }

    /**
     * Enumerates the body objects for all active users on the server.
     * This should only be called from the dobjmgr thread.  The caller had
     * best be certain they know what they're doing, since this should
     * only be necessary for use in rather special circumstances.
     */
    public static Iterator enumerateBodies ()
    {
        return clmgr.enumerateClientObjects();
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

    /** The config key for our list of invocation provider mappings. */
    protected final static String PROVIDERS_KEY = "providers";
}
