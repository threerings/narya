//
// $Id: BuildTestTileSetBundle.java,v 1.2 2001/11/29 20:30:47 mdb Exp $

package com.threerings.media.tools.tile.bundle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.io.PersistenceException;
import com.samskivert.test.TestUtil;

import com.threerings.media.tile.TileSetIDBroker;

public class TileSetBundlerTest
{
    public static void main (String[] args)
    {
        try {
            TileSetIDBroker broker = new DummyTileSetIDBroker();

            // sort out some paths
            String configPath = TestUtil.getResourcePath(CONFIG_PATH);
            String descPath = TestUtil.getResourcePath(BUNDLE_DESC_PATH);
            String targetPath = TestUtil.getResourcePath(TARGET_PATH);

            // create our bundler and get going
            TileSetBundler bundler = new TileSetBundler(configPath);
            File descFile = new File(descPath);
            bundler.createBundle(broker, descFile, targetPath);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Dummy tileset id broker that makes up tileset ids (which are
     * consistent in the course of execution of the application, but not
     * between invocations). */
    protected static class DummyTileSetIDBroker
        extends HashMap
        implements TileSetIDBroker
    {
        public int getTileSetID (String tileSetName)
            throws PersistenceException
        {
            Integer id = (Integer)get(tileSetName);
            if (id == null) {
                id = new Integer(++_nextId);
                put(tileSetName, id);
            }
            return id.intValue();
        }

        public void commit ()
            throws PersistenceException
        {
        }

        protected int _nextId;
    }

    protected static final String CONFIG_PATH =
        "media/tools/tile/bundle/bundler-config.xml";

    protected static final String BUNDLE_DESC_PATH =
        "media/tools/tile/bundle/bundle.xml";

    protected static final String TARGET_PATH =
        "media/tools/tile/bundle/bundle.jar";
}
