//
// $Id: MisoConfig.java,v 1.4 2004/08/27 02:20:05 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
