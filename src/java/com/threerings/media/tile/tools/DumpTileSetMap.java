//
// $Id: DumpTileSetMap.java,v 1.1 2002/06/04 02:50:02 mdb Exp $

package com.threerings.media.tile.tools;

import java.io.File;
import java.util.Iterator;

import com.samskivert.io.PersistenceException;

/**
 * Prints out the tileset mappings in a {@link MapFileTileSetIDBroker}.
 */
public class DumpTileSetMap
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: DumpTileSetMap tileset.map");
            System.exit(-1);
        }

        try {
            MapFileTileSetIDBroker broker =
                new MapFileTileSetIDBroker(new File(args[0]));
            Iterator iter = broker.enumerateMappings();
            while (iter.hasNext()) {
                String tsname = iter.next().toString();
                System.out.println(tsname + " => " +
                                   broker.getTileSetID(tsname));
            }

        } catch (PersistenceException pe) {
            System.err.println("Unable to dump mapping: " + pe);
            System.exit(-1);
        }
    }
}
