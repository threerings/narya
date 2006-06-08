package com.threerings.presents.client {

import com.threerings.util.Config;

/**
 * Provides access to runtime configuration parameters for this package.
 */
public class PresentsPrefs
{
    /** Used to store our preferences. */
    public static const config :Config = new Config("rsrc/config/presents");
}
}
