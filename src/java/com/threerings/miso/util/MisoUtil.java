//
// $Id: MisoUtil.java,v 1.2 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.util;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.samskivert.util.*;
import com.threerings.media.ImageManager;
import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.tile.*;
import com.threerings.resource.ResourceManager;

/**
 * MisoUtil provides miscellaneous routines for applications or other
 * layers that intend to make use of Miso services.
 */
public class MisoUtil
{
    /**
     * Populate the config object with miso configuration values.
     *
     * @param config the Config object to populate.
     */
    public static void bindProperties (Config config) throws IOException
    {
	config.bindProperties("miso", "rsrc/config/miso/miso");
    }

    /**
     * Create a SceneManager object, reading the class name to
     * instantiate from the "miso.scenemgr" config value. 
     *
     * @param config the Config object.
     *
     * @return the new SceneManager object or null if an error occurred.
     */
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

    /**
     * Create a TileManager object.
     *
     * @param config the Config object.
     * @param frame the root frame to which images will be rendered.
     *
     * @return the new TileManager object or null if an error occurred.
     */
    public static TileManager createTileManager (Config config, Frame frame)
    {
	ResourceManager rmgr = createResourceManager();
	ImageManager imgmgr = new ImageManager(rmgr, frame);
	TileSetManager tilesetmgr = createTileSetManager(config, imgmgr);
	TileManager tilemgr = new TileManager(tilesetmgr);

	return tilemgr;
    }

    /**
     * Create a ResourceManager object.
     *
     * @return the new ResourceManager object or null if an error occurred.
     */
    protected static ResourceManager createResourceManager ()
    {
	String root = System.getProperty("root", "");
	String localroot = "file:" + root + "/rsrc";

	try {
	    return new ResourceManager(new URL(localroot));

	} catch (MalformedURLException mue) {
	    Log.warning("Malformed resource manager URL [url=" + localroot +
			", mue=" + mue + "].");
	    return null;
	}
    }	

    /**
     * Create a TileSetManager object, reading the class name to
     * instantiate from the "miso.tilesetmgr" config value. 
     *
     * @param config the Config object.
     * @param imgmgr the ImageManager object from which images are obtained.
     *
     * @return the new TileSetManager object or null if an error occurred.
     */
    protected static TileSetManager
        createTileSetManager (Config config, ImageManager imgmgr)
    {
	TileSetManagerImpl tilesetmgr = null;
	try {
	    tilesetmgr = (TileSetManagerImpl)
		config.instantiateValue("miso.tilesetmgr", DEF_TILESETMGR);
	    tilesetmgr.init(config, imgmgr);

	} catch (Exception e) {
	    Log.warning("Failed to instantiate tileset manager " +
			"[e=" + e + "].");
	}

	return tilesetmgr;
    }

    /** The default SceneManager class name. */
    protected static final String DEF_SCENEMGR =
        CompiledSceneManager.class.getName();

    /** The default TileSetManager class name. */
    protected static final String DEF_TILESETMGR =
        EditableTileSetManager.class.getName();
}
