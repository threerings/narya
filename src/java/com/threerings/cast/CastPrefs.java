//
// $Id: CastPrefs.java,v 1.1 2003/01/15 02:36:59 mdb Exp $

package com.threerings.cast;

import com.samskivert.util.Config;

/**
 * Provides access to runtime configuration parameters for this package
 * and its subpackages.
 */
public class CastPrefs
{
    /** Used to load our preferences from a properties file and map them
     * to the persistent Java preferences repository. */
    public static Config config = new Config("rsrc/config/cast");
}
