//
// $Id: PresentsConfig.java,v 1.2 2002/05/28 22:25:44 mdb Exp $

package com.threerings.presents.server;

import com.samskivert.util.Config;

/**
 * Provides access to the Presents server configuration.
 */
public class PresentsConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/presents/server");

    /**
     * Returns the list of invocation service providers to be registered
     * at server startup.
     */
    public static String[] getProviders ()
    {
        return config.getValue(PROVIDERS_KEY, (String[])null);
    }

    /** The config key for our list of invocation provider mappings. */
    protected final static String PROVIDERS_KEY = "providers";
}
