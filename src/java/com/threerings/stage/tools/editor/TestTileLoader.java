//
// $Id: TestTileLoader.java 9938 2003-06-20 03:55:58Z mdb $

package com.threerings.stage.tools.editor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.ImageIO;

import com.samskivert.util.HashIntMap;

import com.threerings.media.tile.ImageProvider;
import com.threerings.media.tile.SimpleCachingImageProvider;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetIDBroker;

import com.threerings.miso.tile.tools.xml.BaseTileSetRuleSet;
import com.threerings.media.tile.tools.xml.ObjectTileSetRuleSet;
import com.threerings.media.tile.tools.xml.SwissArmyTileSetRuleSet;
import com.threerings.media.tile.tools.xml.XMLTileSetParser;

/**
 * The TestTileLoader handles test tiles. Test tiles are tiles that an
 * artist can load in on-the-fly to see how things look in the scene editor.
 */
public class TestTileLoader implements TileSetIDBroker
{
    /**
     * Construct the TestTileLoader.
     */
    public TestTileLoader ()
    {
        // our xml parser
        _parser = new XMLTileSetParser();
        // add some rulesets
        _parser.addRuleSet("bundle/base", new BaseTileSetRuleSet());
        _parser.addRuleSet("bundle/object", new ObjectTileSetRuleSet());

        // we used to parse fringes, but we don't anymore
        //_parser.addRuleSet("bundle/fringe", new SwissArmyTileSetRuleSet());
    }

    /**
     * Check the specified directory and all its subdirectories for xml files.
     * Each directory should contain at most one xml file, each xml file
     * should specify at most one tileset. That tileset specification
     * will be used to create tilesets for all the .png files in the same
     * directory.
     *
     * @return a HashIntMap containing a TileSetId -> TileSet mapping for
     * all the tilesets we create.
     */
    public HashIntMap loadTestTiles ()
    {
        String directory = EditorConfig.getTestTileDirectory();
        HashIntMap map = new HashIntMap();

        // recurse test directory, making a tileset from the xml file inside
        // and cloning it for each image we find in there.
        File testdir = new File(directory);
        // make sure it's a directory
        if (!testdir.isDirectory()) {
            Log.warning("Test tileset directory is not actually a directory: " +
                directory);
            return map;
        }

        // recursively load all the test tiles
        loadTestTilesFromDir(testdir, map);

        return map;
    }

    /**
     * Load xml tile sets from a directory.
     */
    protected void loadTestTilesFromDir (File directory,
                                         HashIntMap sets)
    {
        // first recurse
        File[] subdirs = directory.listFiles(new FileFilter() {
            public boolean accept (File f) {
                return f.isDirectory();
            }
        });
        for (int ii=0; ii < subdirs.length; ii++) {
            loadTestTilesFromDir(subdirs[ii], sets);
        }

        // now look for the xml file
        String[] xml = directory.list(new FilenameFilter() {
            public boolean accept (File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        for (int ii=0; ii < xml.length; ii++) {
            File xmlfile = new File(directory, xml[ii]);

            HashMap tiles = new HashMap();
            try {
                _parser.loadTileSets(xmlfile, tiles);
            } catch (IOException ioe) {
                Log.warning("Error while parsing " + xmlfile.getPath());
                Log.logStackTrace(ioe);
                continue;
            }

            Iterator iter = tiles.values().iterator();
            while (iter.hasNext()) {
                TileSet ts = (TileSet) iter.next();
                String path = new File(directory, ts.getImagePath()).getPath();

                // before we insert, make sure we can load the image
                if (null != _improv.getTileSetImage(path, null)) {
                    ts.setImageProvider(_improv);
                    ts.setImagePath(path);
                    sets.put(getTileSetID(path), ts);
                }
            }
        }
    }

    /**
     * Generate unique and completely fake tileset IDs that will be stable
     * even after a reload of test tiles.
     */
    public int getTileSetID (String tileSetPath)
    {
        Integer id = (Integer) _idmap.get(tileSetPath);
        if (null == id) {
            id = new Integer(_fakeID--);
            _idmap.put(tileSetPath, id);
        }
        return id.intValue();
    }

    // documentation inherited
    public boolean tileSetMapped (String tilesetPath)
    {
        return _idmap.containsKey(tilesetPath);
    }

    /**
     * Since we're just testing, we don't save these crazy IDs.
     */
    public void commit ()
    {
        // this method does nothing. perhaps it should be called "committee".
    }

    /** The value of the next fakeID we'll hand out. */
    protected int _fakeID = -1;

    /** A mapping of pathname -> tileset id. */
    protected HashMap _idmap = new HashMap();

    /** Our xml parser. */
    protected XMLTileSetParser _parser;

    /** Our image provider. */
    protected ImageProvider _improv = new SimpleCachingImageProvider() {
        protected BufferedImage loadImage (String path)
            throws IOException {
            return ImageIO.read(new File(path));
        }
   };
}
