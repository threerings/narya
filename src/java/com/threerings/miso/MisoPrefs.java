//
// $Id: MisoPrefs.java,v 1.1 2003/01/15 07:41:31 mdb Exp $

package com.threerings.miso;

import com.samskivert.util.Config;

/**
 * Provides access to runtime configuration parameters for the miso
 * package and its subpackages.
 */
public class MisoPrefs
{
    /** Used to load our preferences from a properties file and map them
     * to the persistent Java preferences repository. */
    public static Config config = new Config("rsrc/config/miso");
}
