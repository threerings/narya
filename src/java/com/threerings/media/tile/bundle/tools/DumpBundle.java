//
// $Id: DumpBundle.java,v 1.1 2001/11/29 22:10:54 mdb Exp $

package com.threerings.media.tools.tile.bundle;

import java.io.File;
import java.util.Iterator;

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
        if (args.length < 1) {
            String usage = "Usage: DumpBundle bundle.jar [bundle.jar ...]";
            System.err.println(usage);
            System.exit(-1);
        }

        for (int i = 0; i < args.length; i++) {
            File file = new File(args[i]);
            try {
                ResourceBundle bundle = new ResourceBundle(file);
                TileSetBundle tsb = BundleUtil.extractBundle(bundle);
                Iterator tsids = tsb.enumerateTileSetIds();
                while (tsids.hasNext()) {
                    Integer tsid = (Integer)tsids.next();
                    TileSet set = tsb.getTileSet(tsid.intValue());
                    System.out.println(tsid + " => " + set);
                }

            } catch (Exception e) {
                System.err.println("Error dumping bundle [path=" + args[i] +
                                   ", error=" + e + "].");
                e.printStackTrace();
            }
        }
    }
}
