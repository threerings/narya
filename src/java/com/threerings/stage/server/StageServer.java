//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.stage.server;

import java.awt.Container;

import com.threerings.resource.ResourceManager;

import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.whirled.server.WhirledServer;

import com.threerings.stage.Log;
import com.threerings.stage.data.StageCodes;

/**
 * Extends the Whirled server to provide services needed by the Stage
 * system.
 */
public abstract class StageServer extends WhirledServer
{
    /** A resource manager with which we can load resources in the same
     * manner that the client does (for resources that are used on both
     * the server and client). */
    public ResourceManager rsrcmgr;

    /** Provides access to image resources. */
    public static ImageManager imagemgr;
        
    /** Provides access to our tile repository. */
    public static TileManager tilemgr;

    // documentation inherited
    public void init ()
        throws Exception
    {
        // do the base server initialization
        super.init();

        // create the resource manager
        rsrcmgr = new ResourceManager("rsrc");
        rsrcmgr.initBundles(null, "config/resource/manager.properties", null);

        // create our image manager, tile manager and repository
        imagemgr = new ImageManager(rsrcmgr, null);
        tilemgr = new TileManager(imagemgr);
        tilemgr.setTileSetRepository(
            new BundledTileSetRepository(rsrcmgr, imagemgr,
                                         StageCodes.TILESET_RSRC_SET));

        Log.info("Stage server initialized.");
    }
}
