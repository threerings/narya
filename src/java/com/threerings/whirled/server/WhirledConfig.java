//
// $Id: WhirledConfig.java,v 1.1 2002/03/28 22:32:33 mdb Exp $

package com.threerings.whirled.server;

import com.samskivert.util.Config;

/**
 * Provides access to the Whirled server configuration.
 */
public class WhirledConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/whirled/server");
}
