//
// $Id: EditorConfig.java 1352 2002-03-28 23:55:21Z ray $

package com.threerings.stage.tools.editor;

import com.samskivert.util.Config;

/**
 * Provides access to configuration data for the editor.
 */
public class EditorConfig
{
    /** Provides access to config data for this package. */
    public static Config config = new Config("rsrc/config/stage/tools/editor");

    /**
     * Accessor method for getting the test tile directory.
     */
    public static String getTestTileDirectory ()
    {
        return config.getValue(TESTTILE_KEY, TESTTILE_DEF);
    }

    /**
     * Accessor method for setting the test tile directory.
     */
    public static void setTestTileDirectory (String newvalue)
    {
        config.setValue(TESTTILE_KEY, newvalue);
    }

    private static final String TESTTILE_KEY = "testtiledir";
    private static final String TESTTILE_DEF = ".";
}
