//
// $Id: MisoUtil.java,v 1.9 2001/08/29 18:41:46 shaper Exp $

package com.threerings.miso.util;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.samskivert.util.*;
import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.tile.*;

/**
 * MisoUtil provides miscellaneous routines for applications or other
 * layers that intend to make use of Miso services.
 */
public class MisoUtil
{
    /** The config key prefix for miso properties. */
    public static final String CONFIG_KEY = "miso";

    /**
     * Populate the config object with miso configuration values.
     *
     * @param config the <code>Config</code> object to populate.
     */
    public static void bindProperties (Config config) throws IOException
    {
	config.bindProperties(CONFIG_KEY, "rsrc/config/miso/miso");
    }

    /**
     * Create an <code>XMLFileSceneRepository</code> object, reading the
     * name of the class to instantiate from the config object.
     *
     * @param config the <code>Config</code> object.
     *
     * @return the new scene repository object or null if an error
     * occurred.
     */
    public static XMLFileSceneRepository createSceneRepository (
        Config config, TileManager tilemgr)
    {
	try {
            XMLFileSceneRepository scenerepo = (XMLFileSceneRepository)
                config.instantiateValue(SCENEREPO_KEY, DEF_SCENEREPO);
            scenerepo.init(config, tilemgr);
            return scenerepo;

	} catch (Exception e) {
	    Log.warning("Failed to instantiate scene repository " +
                        "[e=" + e + "].");
	    return null;
	}
    }

    /**
     * Create a <code>TileManager</code> object.
     *
     * @param config the <code>Config</code> object.
     * @param frame the root frame to which images will be rendered.
     *
     * @return the new tile manager object or null if an error occurred.
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
     * Create a <code>ResourceManager</code> object.
     *
     * @return the new resource manager object or null if an error occurred.
     */
    protected static ResourceManager createResourceManager ()
    {
        return new ResourceManager("rsrc");
    }	

    /**
     * Create a <code>TileSetManager</code> object, reading the class
     * name to instantiate from the config object.
     *
     * @param config the <code>Config</code> object.
     * @param imgmgr the <code>ImageManager</code> object from which
     * images are obtained.
     *
     * @return the new tileset manager object or null if an error occurred.
     */
    protected static TileSetManager createTileSetManager (
        Config config, ImageManager imgmgr)
    {
	TileSetManagerImpl tilesetmgr = null;
	try {
	    tilesetmgr = (TileSetManagerImpl)
		config.instantiateValue(TILESETMGR_KEY, DEF_TILESETMGR);
	    tilesetmgr.init(config, imgmgr);

	} catch (Exception e) {
	    Log.warning("Failed to instantiate tileset manager " +
			"[e=" + e + "].");
	}

	return tilesetmgr;
    }

    /** The default scene repository class name. */
    protected static final String DEF_SCENEREPO =
        XMLFileSceneRepository.class.getName();

    /** The default tileset manager class name. */
    protected static final String DEF_TILESETMGR =
        EditableTileSetManager.class.getName();

    /** The config key for the scene repository class. */
    protected static final String SCENEREPO_KEY = CONFIG_KEY + ".scenerepo";

    /** The config key for the tileset manager class. */
    protected static final String TILESETMGR_KEY = CONFIG_KEY + ".tilesetmgr";
}
