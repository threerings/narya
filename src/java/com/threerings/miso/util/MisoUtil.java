//
// $Id: MisoUtil.java,v 1.15 2001/11/18 04:09:23 mdb Exp $

package com.threerings.miso.util;

import java.io.IOException;
import com.samskivert.util.Config;
import com.threerings.miso.Log;

/**
 * The miso util class provides miscellaneous routines for
 * applications or other layers that intend to make use of Miso
 * services.
 */
public class MisoUtil
{
    /** The config key prefix for miso properties. */
    public static final String CONFIG_KEY = "miso";

    /**
     * Populates the config object with miso configuration values.
     *
     * @param config the <code>Config</code> object to populate.
     */
    public static void bindProperties (Config config) throws IOException
    {
	config.bindProperties(CONFIG_KEY, "rsrc/config/miso/miso");
    }

    /**
     * Creates a <code>Config</code> object that contains
     * configuration parameters for miso.
     */
    public static Config createConfig ()
    {
        return createConfig(null, null);
    }

    /**
     * Creates a <code>Config</code> object that contains
     * configuration parameters for miso.  If <code>key</code> and
     * <code>path</code> are non-<code>null</code>, the properties in
     * the given file will additionally be bound to the specified
     * config key namespace.
     */
    public static Config createConfig (String key, String path)
    {
	Config config = new Config();
	try {
            // load the miso config info
	    bindProperties(config);

            if (key != null && path != null) {
                // load the application-specific config info
                config.bindProperties(key, path);
            }

	} catch (IOException ioe) {
	    Log.warning("Error loading config information [e=" + ioe + "].");
	}

	return config;
    }
}
