//
// $Id: BundledTileSetRepositoryTest.java,v 1.1 2001/11/21 02:42:16 mdb Exp $

package com.threerings.media.tile.bundle;

import java.util.Iterator;
import com.threerings.resource.ResourceManager;

public class BundledTileSetRepositoryTest
{
    public static void main (String[] args)
    {
        try {
            ResourceManager rmgr = new ResourceManager(null, "rsrc");
            BundledTileSetRepository repo =
                new BundledTileSetRepository(rmgr, "bundle_test");
            Iterator sets = repo.enumerateTileSets();
            while (sets.hasNext()) {
                System.out.println(sets.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
