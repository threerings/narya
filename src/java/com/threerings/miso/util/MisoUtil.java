//
// $Id: MisoUtil.java,v 1.1 2001/07/21 01:51:10 shaper Exp $

package com.threerings.miso.util;

import java.awt.Frame;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import com.samskivert.util.Config;
import com.threerings.media.ImageManager;
import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.tile.*;
import com.threerings.resource.ResourceManager;

public class MisoUtil
{
    public static void bindProperties (Config config) throws IOException
    {
	config.bindProperties("miso", "rsrc/config/miso/miso");
    }

    public static SceneManager createSceneManager (Config config)
    {
	try {
	    return (SceneManager)
		config.instantiateValue("miso.scenemgr", DEF_SCENEMGR);

	} catch (Exception e) {
	    Log.warning("Failed to instantiate scene manager [e=" + e + "].");
	    return null;
	}
    }

    public static TileManager createTileManager (Config config, Frame frame)
    {
	ResourceManager rmgr = createResourceManager();
	ImageManager imgmgr = new ImageManager(rmgr, frame);
	TileSetManager tilesetmgr = createTileSetManager(config, imgmgr);
	TileManager tilemgr = new TileManager(tilesetmgr);

	// load the tileset descriptions
	String tfile = config.getValue("miso.tilesets", (String)null);
	tilemgr.loadTileSets(tfile);

	return tilemgr;
    }

    protected static ResourceManager createResourceManager ()
    {
	String root = System.getProperty("root", "");
	String localroot = "file:" + root + "/rsrc";

	try {
	    return new ResourceManager(new URL(localroot));

	} catch (MalformedURLException mue) {
	    Log.warning("Malformed resource manager URL [url=" + localroot +
			", mue=" + mue + "].");
	}

	return null;
    }	

    protected static TileSetManager
        createTileSetManager (Config config, ImageManager imgmgr)
    {
	TileSetManager tilesetmgr = null;
	try {
	    tilesetmgr = (TileSetManager)
		config.instantiateValue("miso.tilesetmgr", DEF_TILESETMGR);
	    tilesetmgr.init(imgmgr);

	} catch (Exception e) {
	    Log.warning("Failed to instantiate tileset manager " +
			"[e=" + e + "].");
	}

	return tilesetmgr;
    }

    protected static final String DEF_SCENEMGR = 
        CompiledSceneManager.class.getName();
    protected static final String DEF_TILESETMGR =
        EditableTileSetManager.class.getName();
}
