//
// $Id: BundledTileSetRepositoryTest.java,v 1.5 2002/01/16 03:01:24 mdb Exp $

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
            ResourceManager rmgr = new ResourceManager(
                "rsrc", null, "config/resource/manager.properties");
            ImageManager imgr = new ImageManager(rmgr, null);
            BundledTileSetRepository repo =
                new BundledTileSetRepository(rmgr, imgr, "tilesets");
            Iterator sets = repo.enumerateTileSets();
            while (sets.hasNext()) {
                System.out.println(sets.next());
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
