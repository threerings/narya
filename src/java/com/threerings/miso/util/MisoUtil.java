//
// $Id: MisoUtil.java,v 1.6 2001/08/15 00:00:51 mdb Exp $

package com.threerings.miso.util;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.samskivert.util.*;
import com.threerings.media.ImageManager;
import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
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
     * @param config the <code>Config</code> object to populate.
     */
    public static void bindProperties (Config config) throws IOException
    {
	config.bindProperties("miso", "rsrc/config/miso/miso");
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
                config.instantiateValue("miso.scenerepo", DEF_SCENEREPO);
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
		config.instantiateValue("miso.tilesetmgr", DEF_TILESETMGR);
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
}
