//
// $Id: MisoConfig.java,v 1.2 2003/04/17 19:21:16 mdb Exp $

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
            config.getValue(FINE_GRAN_KEY, DEF_FINE_GRAN),
            config.getValue(SCENE_VWIDTH_KEY, DEF_SCENE_VWIDTH),
            config.getValue(SCENE_VHEIGHT_KEY, DEF_SCENE_VHEIGHT),
            config.getValue(SCENE_OFFSET_Y_KEY, DEF_OFFSET_Y));
    }

    /** The config key for tile width in pixels. */
    protected static final String TILE_WIDTH_KEY = "tile_width";

    /** The config key for tile height in pixels. */
    protected static final String TILE_HEIGHT_KEY = "tile_height";

    /** The config key for tile fine coordinate granularity. */
    protected static final String FINE_GRAN_KEY = "fine_granularity";

    /** The config key for scene view width in tile count. */
    protected static final String SCENE_VWIDTH_KEY = "scene_view_width";

    /** The config key for scene view height in tile count. */
    protected static final String SCENE_VHEIGHT_KEY = "scene_view_height";

    /** The config key for scene width in tile count. */
    protected static final String SCENE_WIDTH_KEY = "scene_width";

    /** The config key for scene height in tile count. */
    protected static final String SCENE_HEIGHT_KEY = "scene_height";

    /** The config key for scene origin vertical offset in tile count. */
    protected static final String SCENE_OFFSET_Y_KEY = "scene_offset_y";

    /** Default scene view parameters. */
    protected static final int DEF_TILE_WIDTH = 64;
    protected static final int DEF_TILE_HEIGHT = 48;
    protected static final int DEF_FINE_GRAN = 4;
    protected static final int DEF_SCENE_VWIDTH = 10;
    protected static final int DEF_SCENE_VHEIGHT = 12;
    protected static final int DEF_SCENE_WIDTH = 22;
    protected static final int DEF_SCENE_HEIGHT = 22;
    protected static final int DEF_OFFSET_Y = -5;
}
