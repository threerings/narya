//
// $Id: MisoUtil.java,v 1.11 2001/10/30 16:16:01 shaper Exp $

package com.threerings.miso.util;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.samskivert.util.*;

import com.threerings.cast.CharacterManager;

import com.threerings.resource.ResourceManager;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileComponentRepository;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.tile.*;

/**
 * The miso util class provides miscellaneous routines for
 * applications or other layers that intend to make use of Miso
 * services.
 */
public class MisoUtil
{
    /** The config key prefix for miso properties. */
    public static final String CONFIG_KEY = "miso";

    /**
     * Populates the config object with miso configuration values.
     *
     * @param config the <code>Config</code> object to populate.
     */
    public static void bindProperties (Config config) throws IOException
    {
	config.bindProperties(CONFIG_KEY, "rsrc/config/miso/miso");
    }

    /**
     * Creates a <code>Config</code> object that contains
     * configuration parameters for miso.
     */
    public static Config createConfig ()
    {
        return createConfig(null, null);
    }

    /**
     * Creates a <code>Config</code> object that contains
     * configuration parameters for miso.  If <code>key</code> and
     * <code>path</code> are non-<code>null</code>, the properties in
     * the given file will additionally be bound to the specified
     * config key namespace.
     */
    public static Config createConfig (String key, String path)
    {
	Config config = new Config();
	try {
            // load the miso config info
	    bindProperties(config);

            if (key != null && path != null) {
                // load the application-specific config info
                config.bindProperties(key, path);
            }

	} catch (IOException ioe) {
	    Log.warning("Error loading config information [e=" + ioe + "].");
	}

	return config;
    }

    /**
     * Creates a <code>CharacterManager</code> object.
     *
     * @param config the <code>Config</code> object.
     * @param tilemgr the tile manager.
     *
     * @return the new character manager object or null if an error
     * occurred.
     */
    public static CharacterManager createCharacterManager (
        Config config, TileManager tilemgr)
    {
        XMLFileComponentRepository crepo =
            new XMLFileComponentRepository(config, tilemgr); 
        CharacterManager charmgr = new CharacterManager(crepo);
        charmgr.setCharacterClass(MisoCharacterSprite.class);
        return charmgr;
    }

    /**
     * Creates an <code>XMLFileSceneRepository</code> object, reading
     * the name of the class to instantiate from the config object.
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
     * Creates a <code>TileManager</code> object.
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
     * Creates a <code>ResourceManager</code> object.
     *
     * @return the new resource manager object or null if an error occurred.
     */
    protected static ResourceManager createResourceManager ()
    {
        return new ResourceManager("rsrc");
    }	

    /**
     * Creates a <code>TileSetManager</code> object, reading the class
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
