//
// $Id: DumpBundle.java,v 1.11 2003/07/28 04:07:30 mdb Exp $

package com.threerings.media.tile.bundle.tools;

import java.io.File;
import java.util.Iterator;

import com.threerings.resource.ResourceManager;
import com.threerings.resource.ResourceBundle;

import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.bundle.BundleUtil;
import com.threerings.media.tile.bundle.TileSetBundle;

/**
 * Dumps the contents of a tileset bundle to stdout (just the serialized
 * object info, not the image data).
 */
public class DumpBundle
{
    public static void main (String[] args)
    {
        boolean dumpTiles = false;

        if (args.length < 1) {
            String usage = "Usage: DumpBundle [-tiles] " +
                "(bundle.jar|tsbundle.dat) [...]";
            System.err.println(usage);
            System.exit(-1);
        }

        // create a resource and image manager in case they want to dump
        // the tiles
        ResourceManager rmgr = new ResourceManager("rsrc");

        for (int i = 0; i < args.length; i++) {
            // oh the hackery
            if (args[i].equals("-tiles")) {
                dumpTiles = true;
                continue;
            }

            File file = new File(args[i]);
            try {
                TileSetBundle tsb = null;
                if (args[i].endsWith(".jar")) {
                    ResourceBundle bundle = new ResourceBundle(file);
                    tsb = BundleUtil.extractBundle(bundle);
                    tsb.init(bundle);
                } else {
                    tsb = BundleUtil.extractBundle(file);
                }

                Iterator tsids = tsb.enumerateTileSetIds();
                while (tsids.hasNext()) {
                    Integer tsid = (Integer)tsids.next();
                    TileSet set = tsb.getTileSet(tsid.intValue());
                    System.out.println(tsid + " => " + set);
                    if (dumpTiles) {
                        for (int t = 0, nn = set.getTileCount(); t < nn; t++) {
                            System.out.println("  " + t + " => " +
                                               set.getTile(t));
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Error dumping bundle [path=" + args[i] +
                                   ", error=" + e + "].");
                e.printStackTrace();
            }
        }
    }
}
