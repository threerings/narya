//
// $Id: PresentsPrefs.java,v 1.1 2003/01/21 00:16:36 mdb Exp $

package com.threerings.presents.client;

import com.samskivert.util.Config;

/**
 * Provides access to runtime configuration parameters for this package
 * and its subpackages.
 */
public class PresentsPrefs
{
    /** Used to load our preferences from a properties file and map them
     * to the persistent Java preferences repository. */
    public static Config config = new Config("rsrc/config/presents");
}
