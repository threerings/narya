//
// $Id: MisoUtil.java,v 1.16 2001/11/29 00:19:03 mdb Exp $

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
}
