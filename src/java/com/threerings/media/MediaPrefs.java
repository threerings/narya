//
// $Id: MediaPrefs.java,v 1.1 2003/01/15 00:47:29 mdb Exp $

package com.threerings.media;

import com.samskivert.util.Config;

/**
 * Provides access to runtime configuration parameters for the media
 * package and its subpackages.
 */
public class MediaPrefs
{
    /** Used to load our preferences from a properties file and map them
     * to the persistent Java preferences repository. */
    public static Config config = new Config("rsrc/config/media");
}
