//
// $Id: MiCasaConfig.java,v 1.1 2002/03/28 22:32:32 mdb Exp $

package com.threerings.micasa.server;

import com.samskivert.util.Config;

/**
 * Provides access to the MiCasa server configuration.
 */
public class MiCasaConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/micasa/server");
}
