//
// $Id: XMLSceneRepository.java,v 1.10 2001/08/29 18:41:46 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.samskivert.util.Config;
import com.threerings.media.tile.TileManager;
import com.threerings.miso.Log;
import com.threerings.miso.scene.MisoScene;
import com.threerings.miso.util.MisoUtil;

/**
 * The <code>XMLFileSceneRepository</code> provides a mechanism for
 * reading scenes from and writing scenes to XML files.  These files will
 * comprise the template scene files from which actual runtime game scenes
 * will be constructed.
 */
public class XMLFileSceneRepository
{
    /**
     * Initialize the XMLFileSceneRepository with the given config and
     * tile manager objects.
     *
     * @param config the config object.
     * @param tilemgr the tile manager object.
     */
    public void init (Config config, TileManager tilemgr)
    {
        // keep these for later
        _config = config;
        _tilemgr = tilemgr;

	// get path-related information
	_root = System.getProperty("root", "");
        _sceneRoot = _config.getValue(SCENEROOT_KEY, DEF_SCENEROOT);

	// create the parser and writer objects
        _parser = new XMLSceneParser(_tilemgr);
        _writer = new XMLSceneWriter();
    }

    /**
     * Return the path to the scene root directory.
     */
    public String getScenePath ()
    {
	return _root + File.separator + _sceneRoot + File.separator;
    }

    /**
     * Loads and returns a miso scene object for the scene described in
     * the specified XML file. The filename should be relative to the
     * scene root directory.
     *
     * @param fname the full pathname to the file.
     * @return the scene object.
     */
    public MisoScene loadScene (String fname) throws IOException
    {
	String path = getScenePath() + fname;
	Log.info("Loading scene [path=" + path + "].");

        return _parser.loadScene(path);
    }

    /**
     * Writes a scene to the specified file in the scene root
     * directory in XML format.  The filename should be relative to
     * the scene root directory.
     *
     * @param scene the scene to save.
     * @param fname the file to write the scene to.
     */
    public void saveScene (MisoScene scene, String fname) throws IOException
    {
	String path = getScenePath() + fname;
	Log.info("Saving scene [path=" + path + "].");

        _writer.saveScene(scene, path);
    }

    /** The config object. */
    protected Config _config;

    /** The tile manager from which the scenes obtain their tiles. */
    protected TileManager _tilemgr;

    /** The main program absolute root directory. */
    protected String _root;

    /** The root scene directory path. */
    protected String _sceneRoot;

    /** The parser object for reading scenes from files. */
    protected XMLSceneParser _parser;

    /** The writer object for writing scenes to files. */
    protected XMLSceneWriter _writer;

    /** The config key for the root scene directory. */
    protected static final String SCENEROOT_KEY =
	MisoUtil.CONFIG_KEY + ".sceneroot";

    /** The default root scene directory path. */
    protected static final String DEF_SCENEROOT = "rsrc/scenes";
}
