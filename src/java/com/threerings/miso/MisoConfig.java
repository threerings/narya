//
// $Id: MisoConfig.java,v 1.3 2003/04/19 22:37:57 mdb Exp $

package com.threerings.miso;

import com.samskivert.util.Config;

import com.threerings.miso.util.MisoSceneMetrics;

/**
 * Provides access to the Miso configuration.
 */
public class MisoConfig
{
    /** Provides access to configuration data for this package. */
    public static Config config = new Config("rsrc/config/miso/miso");

    /**
     * Creates scene metrics with information obtained from the deployed
     * config file.
     */
    public static MisoSceneMetrics getSceneMetrics ()
    {
        return new MisoSceneMetrics(
            config.getValue(TILE_WIDTH_KEY, DEF_TILE_WIDTH),
            config.getValue(TILE_HEIGHT_KEY, DEF_TILE_HEIGHT),
            config.getValue(FINE_GRAN_KEY, DEF_FINE_GRAN));
    }

    /** The config key for tile width in pixels. */
    protected static final String TILE_WIDTH_KEY = "tile_width";

    /** The config key for tile height in pixels. */
    protected static final String TILE_HEIGHT_KEY = "tile_height";

    /** The config key for tile fine coordinate granularity. */
    protected static final String FINE_GRAN_KEY = "fine_granularity";

    /** Default scene view parameters. */
    protected static final int DEF_TILE_WIDTH = 64;
    protected static final int DEF_TILE_HEIGHT = 48;
    protected static final int DEF_FINE_GRAN = 4;
}
