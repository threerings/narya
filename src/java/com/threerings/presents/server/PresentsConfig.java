//
// $Id: PresentsConfig.java,v 1.1 2002/03/28 22:32:32 mdb Exp $

package com.threerings.presents.server;

import com.samskivert.util.Config;

/**
 * Provides access to the Presents server configuration.
 */
public class PresentsConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/presents/server");
}
