//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.samskivert.util.PrefsConfig;

/**
 * Provides access to runtime configuration parameters for this package
 * and its subpackages.
 */
public class PresentsPrefs
{
    /** Used to load our preferences from a properties file and map them
     * to the persistent Java preferences repository. */
    public static PrefsConfig config = new PrefsConfig("rsrc/config/presents");
}
