//
// $Id: CrowdConfig.java,v 1.1 2002/03/28 22:32:31 mdb Exp $

package com.threerings.crowd.server;

import com.samskivert.util.Config;

/**
 * Provides access to the Crowd server configuration.
 */
public class CrowdConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/crowd/server");
}
