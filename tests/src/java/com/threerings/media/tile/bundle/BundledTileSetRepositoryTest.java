//
// $Id: BundledTileSetRepositoryTest.java,v 1.8 2003/01/13 22:57:45 mdb Exp $

package com.threerings.media.tile.bundle;

import java.util.Iterator;

import com.threerings.media.image.ImageManager;
import com.threerings.resource.ResourceManager;

import junit.framework.Test;
import junit.framework.TestCase;

public class BundledTileSetRepositoryTest extends TestCase
{
    public BundledTileSetRepositoryTest ()
    {
        super(BundledTileSetRepositoryTest.class.getName());
    }

    public void runTest ()
    {
        try {
            ResourceManager rmgr = new ResourceManager("rsrc");
            rmgr.initBundles(null, "config/resource/manager.properties", null);
            BundledTileSetRepository repo = new BundledTileSetRepository(
                rmgr, new ImageManager(rmgr, null), "tilesets");
            Iterator sets = repo.enumerateTileSets();
            while (sets.hasNext()) {
                sets.next();
//                 System.out.println(sets.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite ()
    {
        return new BundledTileSetRepositoryTest();
    }

    public static void main (String[] args)
    {
        BundledTileSetRepositoryTest test =
            new BundledTileSetRepositoryTest();
        test.runTest();
    }
}
