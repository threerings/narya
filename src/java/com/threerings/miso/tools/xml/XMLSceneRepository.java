//
// $Id: XMLSceneRepository.java,v 1.2 2001/07/24 19:15:51 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.IOException;
import java.util.ArrayList;

import com.samskivert.util.Config;
import com.threerings.miso.Log;
import com.threerings.miso.scene.Scene;
import com.threerings.miso.scene.SceneRepositoryImpl;
import com.threerings.miso.tile.TileManager;

/**
 * The XMLFileSceneRepository provides a mechanism for reading scenes
 * from and writing scenes to XML files.  These files will comprise
 * the template scene files from which actual runtime game scenes will
 * be constructed.
 */
public class XMLFileSceneRepository extends SceneRepositoryImpl
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
        super.init(config, tilemgr);
        _root = _config.getValue(CONFIG_ROOT, DEF_ROOT);
        _parser = new XMLSceneParser();
        _writer = new XMLSceneWriter();
    }

    /**
     * Loads and returns a Scene object for the scene described in the
     * specified XML file.
     *
     * @param fname the full pathname to the file.
     * @return the Scene object.
     */
    public Scene loadScene (String fname) throws IOException
    {
        return _parser.loadScene(fname);
    }

    /**
     * Writes a scene to the specified file in the scene root
     * directory in XML format.
     *
     * @param scene the scene to save.
     * @param fname the file to write the scene to.
     */
    public void saveScene (Scene scene, String fname) throws IOException
    {
        _writer.saveScene(scene, fname);
    }

    /** The config key for the root scene directory. */
    protected static final String CONFIG_ROOT = "miso.sceneroot";

    /** The default root scene directory path. */
    protected static final String DEF_ROOT = "rsrc/scenes";

    /** The root scene directory path. */
    protected String _root;

    /** The parser object for reading scenes from files. */
    protected XMLSceneParser _parser;

    /** The writer object for writing scenes to files. */
    protected XMLSceneWriter _writer;
}
