//
// $Id: MisoConfig.java,v 1.1 2002/03/28 22:32:32 mdb Exp $

package com.threerings.miso;

import com.samskivert.util.Config;

/**
 * Provides access to the Miso configuration.
 */
public class MisoConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/miso/miso");
}
