//
// $Id: BundledTileSetRepositoryTest.java,v 1.7 2002/07/19 20:13:30 shaper Exp $

package com.threerings.media.tile.bundle;

import java.util.Iterator;
import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;

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
            ImageManager imgr = new ImageManager(rmgr, null);
            BundledTileSetRepository repo =
                new BundledTileSetRepository(rmgr, imgr, "tilesets");
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
