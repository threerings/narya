//
// $Id: XMLSceneRepository.java,v 1.13 2001/11/02 02:52:16 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.samskivert.util.Config;
import com.threerings.media.tile.TileManager;
import com.threerings.miso.Log;
import com.threerings.miso.scene.IsoSceneViewModel;
import com.threerings.miso.scene.MisoScene;
import com.threerings.miso.scene.EditableMisoScene;
import com.threerings.miso.util.MisoUtil;

/**
 * The xml scene repository provides a mechanism for reading scenes
 * from and writing scenes to XML files.  These files are template
 * scene files from which actual runtime game scenes will be
 * constructed.
 */
public class XMLSceneRepository
{
    /**
     * Initializes the xml scene repository.
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

	// create an iso scene view model detailing scene dimensions
	_model = new IsoSceneViewModel(config);

	// construct the scene parser and writer objects for later use
        _parser = new XMLSceneParser(_model, _tilemgr);
        _writer = new XMLSceneWriter(_model);
    }

    /**
     * Returns the path to the scene root directory.
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
    public EditableMisoScene loadScene (String fname) throws IOException
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

    /** The iso scene view data model. */
    protected IsoSceneViewModel _model;

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
