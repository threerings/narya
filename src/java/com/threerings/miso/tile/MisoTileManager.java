//
// $Id: MisoTileManager.java,v 1.6 2004/08/27 02:20:07 mdb Exp $
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

package com.threerings.miso.tile;

import java.io.IOException;
import java.io.InputStream;

import com.samskivert.io.StreamUtil;

import com.threerings.resource.ResourceManager;
import com.threerings.util.CompiledConfig;

import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;

/**
 * Extends the basic tile manager and provides support for automatically
 * generating fringes in between different types of base tiles in a scene.
 */
public class MisoTileManager extends TileManager
{
    /**
     * Creates a tile manager and provides it with a reference to the
     * image manager from which it will load tileset images.
     *
     * @param imgr the image manager via which the tile manager will
     * decode and cache images.
     */
    public MisoTileManager (ResourceManager rmgr, ImageManager imgr)
    {
        super(imgr);

        // look for a fringe configuration in the appropriate place
        InputStream in = null;
        try {
            in = rmgr.getResource(FRINGE_CONFIG_PATH);
            FringeConfiguration config = (FringeConfiguration)
                CompiledConfig.loadConfig(in);

            // if we've found it, create our auto fringer with it
            _fringer = new AutoFringer(config, imgr, this);

        } catch (IOException ioe) {
            Log.warning("Unable to load fringe configuration " +
                        "[path=" + FRINGE_CONFIG_PATH +
                        ", error=" + ioe + "].");

        } finally {
            StreamUtil.close(in);
        }
    }

    /**
     * Returns the auto fringer that has been configured for use by this
     * tile manager. This will only be valid if this tile manager has been
     * provided with a miso tileset repository via {@link
     * #setTileSetRepository}.
     */
    public AutoFringer getAutoFringer ()
    {
        return _fringer;
    }

    /** The entity that performs the automatic fringe layer generation. */
    protected AutoFringer _fringer;

    /** The path (in the classpath) to the serialized fringe
     * configuration. */
    protected static final String FRINGE_CONFIG_PATH =
        "config/miso/tile/fringeconf.dat";
}
